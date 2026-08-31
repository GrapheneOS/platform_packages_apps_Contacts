package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Identity
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import com.android.contacts.data.connectedapps.model.ConnectedApp
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.domain.contactdetails.model.CONTACT_DATA_ITEM_PRIORITY
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.util.CanVideoCall
import com.android.contacts.domain.util.IsCallWithNoteSupported
import com.android.contacts.domain.util.IsDeviceVoiceCapable
import com.android.contacts.domain.util.IsSipCallingSupported
import com.android.contacts.model.dataitem.CustomDataItem
import com.android.contacts.util.core.extension.trimmedOrNull
import javax.inject.Inject

internal fun interface BuildContactDetailsCards {
    operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards
}

internal class BuildContactDetailsCardsImpl @Inject constructor(
    private val isDeviceVoiceCapable: IsDeviceVoiceCapable,
    private val isCallWithNoteSupported: IsCallWithNoteSupported,
    private val canVideoCall: CanVideoCall,
    private val isSipCallingSupported: IsSipCallingSupported,
    private val connectedAppsRepository: ConnectedAppsRepository,
) : BuildContactDetailsCards {

    override operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards {
        val headerNicknames = headerNicknames(details)
        val headerOrganizations = headerOrganizations(details)
        val groupMemberships = groupMemberships(details)
        val headerDataIds = (headerNicknames + headerOrganizations + groupMemberships)
            .map { dataItem -> dataItem.id }
            .toSet()

        val groups = details.dataItems
            .filterNot { dataItem -> dataItem.id in headerDataIds }
            .groupBy { dataItem -> dataItem.mimeType }
            .mapValues { group -> group.value.sortedWith(CONTACT_DATA_ITEM_PRIORITY) }

        val groupsByApp = buildContactCard(
            groups,
            details,
            prioritizedMimeType
        ).groupBy(::connectedApp)

        return ContactDetailsCards(
            contactCard = groupsByApp[NO_CONNECTED_APP].orEmpty(),
            connectedApps = buildConnectedApps(groupsByApp),
            notes = buildNotes(groups, details),
            headerNicknames = headerNicknames.mapNotNull { dataItem -> dataItem.name },
            headerOrganizations = headerOrganizations.map(::organizationParts),
            groups = details.groups,
        )
    }

    private fun headerNicknames(details: ContactDetails): List<ContactDataItem.Nickname> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Nickname>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .filter { dataItem ->
                !dataItem.name.isNullOrBlank() && !duplicatesDisplayName(dataItem, details)
            }
    }

    private fun groupMemberships(details: ContactDetails): List<ContactDataItem> {
        return details.dataItems.filter { dataItem ->
            dataItem.mimeType == GroupMembership.CONTENT_ITEM_TYPE
        }
    }

    private fun organizationParts(organization: ContactDataItem.Organization): List<String> {
        return listOfNotNull(
            organization.title,
            organization.department,
            organization.company,
        ).mapNotNull { part -> part.trimmedOrNull() }
    }

    private fun headerOrganizations(details: ContactDetails): List<ContactDataItem.Organization> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Organization>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .filter { dataItem -> organizationParts(dataItem).isNotEmpty() }
    }

    private fun buildContactCard(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): List<ContactEntryGroup> {
        return groups.keys
            .filterNot { mimeType -> mimeType == Note.CONTENT_ITEM_TYPE }
            .sortedBy { mimeType -> mimeTypeRank(mimeType, prioritizedMimeType) }
            .map { mimeType -> toGroup(mimeType, groups.getValue(mimeType), details) }
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun buildConnectedApps(
        groupsByApp: Map<ConnectedApp?, List<ContactEntryGroup>>,
    ): List<ContactConnectedApp> {
        return groupsByApp.mapNotNull { (app, appGroups) ->
            app?.let {
                ContactConnectedApp(
                    app = app,
                    entries = appGroups.flatMap(ContactEntryGroup::entries),
                )
            }
        }
    }

    private fun connectedApp(group: ContactEntryGroup): ConnectedApp? {
        val entry = group.entries.firstOrNull() ?: return null
        if (entry.kind != ContactEntryKind.OTHER) {
            return null
        }

        val action = entry.actions.primary
        if (action !is ContactEntryAction.ViewDataItem) {
            return null
        }

        return connectedAppsRepository.getConnectedApp(
            dataId = action.dataId,
            mimeType = action.mimeType,
        )
    }

    private fun buildNotes(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
    ): List<ContactEntryGroup> {
        val notes = groups[Note.CONTENT_ITEM_TYPE] ?: return emptyList()

        return listOf(toGroup(Note.CONTENT_ITEM_TYPE, notes, details))
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun mimeTypeRank(
        mimeType: String,
        prioritizedMimeType: String?,
    ): Int {
        if (mimeType == prioritizedMimeType) {
            return PRIORITIZED_RANK
        }

        return when (val index = LEADING_MIME_TYPES.indexOf(mimeType)) {
            NOT_FOUND -> LEADING_MIME_TYPES.size
            else -> index
        }
    }

    private fun toGroup(
        mimeType: String,
        dataItems: List<ContactDataItem>,
        details: ContactDetails,
    ): ContactEntryGroup {
        val defaultDataItemId = defaultDataItemId(dataItems)

        return ContactEntryGroup(
            mimeType = mimeType,
            entries = dataItems.mapNotNull { dataItem ->
                toEntry(dataItem, details, defaultDataItemId)
            },
        )
    }

    private fun defaultDataItemId(dataItems: List<ContactDataItem>): Long? {
        return dataItems
            .firstOrNull { dataItem ->
                dataItem.isSuperPrimary || dataItem.isPrimary
            }?.id
    }

    private fun toEntry(
        dataItem: ContactDataItem,
        details: ContactDetails,
        defaultDataItemId: Long?,
    ): ContactEntry? {
        val content = toContent(dataItem, details)?.takeIf { entry ->
            !entry.isEmpty()
        } ?: return null

        return ContactEntry(
            id = dataItem.id,
            mimeType = dataItem.mimeType,
            kind = dataItem.kind(),
            isSuperPrimary = dataItem.isSuperPrimary,
            isDefault = dataItem.id == defaultDataItemId,
            header = content.header,
            subHeader = content.subHeader,
            text = content.text,
            copyText = content.copyText,
            copyLabel = content.copyLabel ?: content.header,
            actions = ContactEntryActions(
                primary = content.primaryAction,
                alternate = content.alternateAction,
                third = content.thirdAction,
            ),
        )
    }

    private fun ContactDataItem.kind(): ContactEntryKind {
        return when (this) {
            is ContactDataItem.Phone -> ContactEntryKind.PHONE
            is ContactDataItem.SipAddress -> ContactEntryKind.SIP_ADDRESS
            is ContactDataItem.Email -> ContactEntryKind.EMAIL
            is ContactDataItem.Postal -> ContactEntryKind.POSTAL
            is ContactDataItem.Im -> ContactEntryKind.IM
            is ContactDataItem.Organization -> ContactEntryKind.ORGANIZATION
            is ContactDataItem.Nickname -> ContactEntryKind.NICKNAME
            is ContactDataItem.Note -> ContactEntryKind.NOTE
            is ContactDataItem.Website -> ContactEntryKind.WEBSITE
            is ContactDataItem.Relation -> ContactEntryKind.RELATION
            is ContactDataItem.Custom -> ContactEntryKind.CUSTOM_FIELD
            is ContactDataItem.StructuredName -> ContactEntryKind.OTHER

            is ContactDataItem.Event -> when {
                isBirthday -> ContactEntryKind.BIRTHDAY
                else -> ContactEntryKind.EVENT
            }

            is ContactDataItem.Generic -> when (mimeType) {
                GroupMembership.CONTENT_ITEM_TYPE -> ContactEntryKind.GROUP
                Identity.CONTENT_ITEM_TYPE -> ContactEntryKind.IDENTITY
                else -> ContactEntryKind.OTHER
            }
        }
    }

    private fun toContent(
        dataItem: ContactDataItem,
        details: ContactDetails,
    ): EntryContent? {
        return when (dataItem) {
            is ContactDataItem.Phone -> phoneContent(dataItem)
            is ContactDataItem.Email -> emailContent(dataItem)
            is ContactDataItem.Postal -> postalContent(dataItem)
            is ContactDataItem.SipAddress -> sipAddressContent(dataItem)
            is ContactDataItem.Im -> imContent(dataItem)
            else -> aboutContent(dataItem, details)
        }
    }

    private fun aboutContent(
        dataItem: ContactDataItem,
        details: ContactDetails,
    ): EntryContent? {
        return when (dataItem) {
            is ContactDataItem.Organization -> organizationContent(dataItem)
            is ContactDataItem.Nickname -> nicknameContent(dataItem, details)
            is ContactDataItem.Note -> noteContent(dataItem)
            is ContactDataItem.Website -> websiteContent(dataItem)
            is ContactDataItem.Event -> eventContent(dataItem)
            is ContactDataItem.Relation -> relationContent(dataItem)
            is ContactDataItem.Custom -> customContent(dataItem)
            is ContactDataItem.Generic -> genericContent(dataItem)
            is ContactDataItem.StructuredName -> null

            is ContactDataItem.Phone,
            is ContactDataItem.Email,
            is ContactDataItem.Postal,
            is ContactDataItem.SipAddress,
            is ContactDataItem.Im,
            -> null
        }
    }

    private fun phoneContent(dataItem: ContactDataItem.Phone): EntryContent? {
        val number = dataItem.number
        if (number.isNullOrEmpty()) {
            return null
        }

        val header = dataItem.displayString.orEmpty()

        return EntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeLabel,
            copyText = header,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
            primaryAction = callAction(number),
            alternateAction = ContactEntryAction.Sms(number),
            thirdAction = thirdPhoneAction(dataItem, number),
        )
    }

    private fun callAction(number: String): ContactEntryAction? {
        return when {
            isDeviceVoiceCapable() -> ContactEntryAction.Call(number)
            else -> null
        }
    }

    private fun thirdPhoneAction(
        dataItem: ContactDataItem.Phone,
        number: String,
    ): ContactEntryAction? {
        return when {
            isCallWithNoteSupported() -> ContactEntryAction.CallWithNote(
                number = number,
                formattedNumber = dataItem.formattedNumber,
                numberLabel = dataItem.typeLabel,
            )

            canVideoCall(dataItem.isCarrierVideoCallCapable) -> {
                ContactEntryAction.VideoCall(number)
            }

            else -> null
        }
    }

    private fun emailContent(dataItem: ContactDataItem.Email): EntryContent? {
        if (dataItem.data.isNullOrEmpty()) {
            return null
        }

        val header = dataItem.address.orEmpty()

        return EntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeLabel,
            copyText = header,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.EMAIL),
            primaryAction = ContactEntryAction.SendEmail(header),
        )
    }

    private fun postalContent(dataItem: ContactDataItem.Postal): EntryContent? {
        if (dataItem.formattedAddress.isNullOrEmpty()) {
            return null
        }

        return EntryContent(
            header = ContactEntryText.Value(dataItem.formattedAddress),
            text = dataItem.typeLabel,
            copyText = dataItem.formattedAddress,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.POSTAL),
            primaryAction = ContactEntryAction.ShowOnMap(dataItem.formattedAddress),
            alternateAction = ContactEntryAction.ShowDirections(dataItem.formattedAddress),
        )
    }

    private fun sipAddressContent(dataItem: ContactDataItem.SipAddress): EntryContent? {
        if (dataItem.address.isNullOrEmpty()) {
            return null
        }

        return EntryContent(
            header = ContactEntryText.Value(dataItem.address),
            text = dataItem.typeLabel,
            copyText = dataItem.address,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
            primaryAction = sipCallAction(dataItem.address),
        )
    }

    private fun sipCallAction(address: String): ContactEntryAction? {
        return when {
            isSipCallingSupported() -> ContactEntryAction.SipCall(address)
            else -> null
        }
    }

    private fun imContent(dataItem: ContactDataItem.Im): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.data.orEmpty()),
            subHeader = imLabel(dataItem),
            copyText = dataItem.data,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.IM),
            primaryAction = chatAction(dataItem),
        )
    }

    private fun imLabel(dataItem: ContactDataItem.Im): ContactEntryText {
        val protocolLabel = dataItem.protocolLabel

        return when {
            protocolLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.IM)
            else -> ContactEntryText.Value(protocolLabel)
        }
    }

    private fun chatAction(dataItem: ContactDataItem.Im): ContactEntryAction? {
        val data = dataItem.data
        if (data.isNullOrEmpty()) {
            return null
        }

        return ContactEntryAction.OpenChat(
            data = data,
            protocol = dataItem.protocol,
            customProtocol = dataItem.customProtocol,
        )
    }

    private fun organizationContent(dataItem: ContactDataItem.Organization): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.formattedCompany.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
            copyText = dataItem.formattedCompany,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
        )
    }

    private fun nicknameContent(
        dataItem: ContactDataItem.Nickname,
        details: ContactDetails,
    ): EntryContent? {
        if (duplicatesDisplayName(dataItem, details)) {
            return null
        }

        return EntryContent(
            header = ContactEntryText.Value(dataItem.name.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.NICKNAME),
            copyText = dataItem.name,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.NICKNAME),
        )
    }

    private fun duplicatesDisplayName(
        dataItem: ContactDataItem.Nickname,
        details: ContactDetails,
    ): Boolean {
        val isNameRawContact = details.nameRawContactId == dataItem.rawContactId

        return isNameRawContact &&
            details.displayNameSource == ContactDisplayNameSource.NICKNAME
    }

    private fun noteContent(dataItem: ContactDataItem.Note): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.note.orEmpty()),
            copyText = dataItem.note,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.NOTE),
        )
    }

    private fun websiteContent(dataItem: ContactDataItem.Website): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.url.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            copyText = dataItem.url,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            primaryAction = dataItem.url?.let(ContactEntryAction::OpenUrl),
        )
    }

    private fun eventContent(dataItem: ContactDataItem.Event): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.formattedDate.orEmpty()),
            subHeader = eventLabel(dataItem),
            copyText = dataItem.formattedDate,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.EVENT),
            primaryAction = eventDateAction(dataItem),
        )
    }

    private fun eventLabel(dataItem: ContactDataItem.Event): ContactEntryText {
        val typeLabel = dataItem.typeLabel

        return when {
            typeLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.EVENT)
            else -> ContactEntryText.Value(typeLabel)
        }
    }

    private fun eventDateAction(dataItem: ContactDataItem.Event): ContactEntryAction? {
        val date = dataItem.displayString
        if (date.isNullOrEmpty()) {
            return null
        }

        return ContactEntryAction.ShowEventDate(
            date = date,
            isRecurringAnnually = dataItem.isRecurringAnnually,
        )
    }

    private fun relationContent(dataItem: ContactDataItem.Relation): EntryContent {
        return EntryContent(
            header = ContactEntryText.Value(dataItem.name.orEmpty()),
            subHeader = relationLabel(dataItem),
            copyText = dataItem.name,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.RELATION),
            primaryAction = dataItem.displayString
                ?.takeIf { name -> name.isNotEmpty() }
                ?.let(ContactEntryAction::SearchContacts),
        )
    }

    private fun relationLabel(dataItem: ContactDataItem.Relation): ContactEntryText {
        val typeLabel = dataItem.typeLabel

        return when {
            typeLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.RELATION)
            else -> ContactEntryText.Value(typeLabel)
        }
    }

    private fun customContent(dataItem: ContactDataItem.Custom): EntryContent {
        val summary = dataItem.summary

        return EntryContent(
            header = ContactEntryText.Value(dataItem.content.orEmpty()),
            subHeader = when {
                summary.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD)
                else -> ContactEntryText.Value(summary)
            },
            copyText = dataItem.content,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD),
        )
    }

    private fun genericContent(dataItem: ContactDataItem.Generic): EntryContent {
        val header = dataItem.displayString.orEmpty()

        return EntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeColumn,
            copyText = header,
            copyLabel = ContactEntryText.Value(dataItem.mimeType),
            primaryAction = ContactEntryAction.ViewDataItem(
                dataId = dataItem.id,
                mimeType = dataItem.mimeType,
            ),
        )
    }

    private data class EntryContent(
        val header: ContactEntryText?,
        val subHeader: ContactEntryText? = null,
        val text: String? = null,
        val copyText: String? = null,
        val copyLabel: ContactEntryText? = null,
        val primaryAction: ContactEntryAction? = null,
        val alternateAction: ContactEntryAction? = null,
        val thirdAction: ContactEntryAction? = null,
    ) {
        fun isEmpty(): Boolean {
            return isTextEmpty(header) && isTextEmpty(subHeader) && text.isNullOrEmpty()
        }

        private fun isTextEmpty(entryText: ContactEntryText?): Boolean {
            return when (entryText) {
                null -> true
                is ContactEntryText.Label -> false
                is ContactEntryText.Value -> entryText.text.isEmpty()
            }
        }
    }

    private companion object {
        val NO_CONNECTED_APP: ConnectedApp? = null

        const val NOT_FOUND = -1
        const val PRIORITIZED_RANK = -1

        val LEADING_MIME_TYPES = listOf(
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
            Website.CONTENT_ITEM_TYPE,
            SipAddress.CONTENT_ITEM_TYPE,
            Relation.CONTENT_ITEM_TYPE,
            Im.CONTENT_ITEM_TYPE,
            Identity.CONTENT_ITEM_TYPE,
            CustomDataItem.MIMETYPE_CUSTOM_FIELD,
        )
    }
}
