package com.android.contacts.data.contactdetails.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.DisplayNameSources as Sources
import com.android.contacts.data.contactdetails.model.ContactAccount
import com.android.contacts.data.contactdetails.model.ContactCapabilities
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactGroup
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.detail.ContactDisplayUtils
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.Contact
import com.android.contacts.model.RawContact
import com.android.contacts.model.account.AccountType
import com.android.contacts.model.account.FallbackAccountType
import com.android.contacts.model.account.SimAccountType
import com.android.contacts.model.dataitem.CustomDataItem
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.DataKind
import com.android.contacts.model.dataitem.EmailDataItem
import com.android.contacts.model.dataitem.EventDataItem
import com.android.contacts.model.dataitem.GroupMembershipDataItem
import com.android.contacts.model.dataitem.ImDataItem
import com.android.contacts.model.dataitem.NicknameDataItem
import com.android.contacts.model.dataitem.NoteDataItem
import com.android.contacts.model.dataitem.OrganizationDataItem
import com.android.contacts.model.dataitem.PhoneDataItem
import com.android.contacts.model.dataitem.RelationDataItem
import com.android.contacts.model.dataitem.SipAddressDataItem
import com.android.contacts.model.dataitem.StructuredNameDataItem
import com.android.contacts.model.dataitem.StructuredPostalDataItem
import com.android.contacts.model.dataitem.WebsiteDataItem
import com.android.contacts.quickcontact.DirectoryContactUtil
import com.android.contacts.quickcontact.InvisibleContactUtil
import com.android.contacts.util.DateUtils
import com.android.contacts.util.core.extension.trimmedOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import javax.inject.Inject

internal interface ContactDetailsMapper {
    fun map(
        contact: Contact,
        excludedMimeTypes: Set<String>,
    ): ContactDetails
}

internal class ContactDetailsMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val accountTypeManager: AccountTypeManager,
    private val dataItemCollapser: DataItemCollapser,
) : ContactDetailsMapper {

    override fun map(
        contact: Contact,
        excludedMimeTypes: Set<String>,
    ): ContactDetails {
        return ContactDetails(
            contactId = contact.id,
            lookupKey = contact.lookupKey,
            lookupUri = contact.lookupUri,
            nameRawContactId = contact.nameRawContactId,
            displayName = contact.displayName,
            alternativeDisplayName = contact.altDisplayName,
            phoneticName = contact.phoneticName,
            displayNameSource = mapDisplayNameSource(contact.displayNameSource),
            isStarred = contact.starred,
            photoId = contact.photoId,
            photo = mapPhoto(contact),
            isSendToVoicemail = contact.isSendToVoicemail,
            customRingtone = contact.customRingtone,
            groups = mapGroups(contact),
            accounts = mapAccounts(contact),
            dataItems = mapDataItems(contact, excludedMimeTypes),
            capabilities = mapCapabilities(contact),
        )
    }

    private fun mapGroups(contact: Contact): List<ContactGroup> {
        val titles = contact.groupMetaData
            .orEmpty()
            .filterNot { group ->
                group.favorites || group.defaultGroup
            }
            .associateBy(
                { group -> group.groupId },
                { group -> group.groupName },
            )

        return contact.rawContacts
            .orEmpty()
            .flatMap { rawContact -> rawContact.dataItems.orEmpty() }
            .filterIsInstance<GroupMembershipDataItem>()
            .mapNotNull { dataItem -> dataItem.groupRowId }
            .distinct()
            .mapNotNull { groupId ->
                mapContactGroup(groupId, titles[groupId])
            }
    }

    private fun mapContactGroup(
        groupId: Long,
        title: String?,
    ): ContactGroup? {
        val groupTitle = title?.trimmedOrNull() ?: return null

        return ContactGroup(
            id = groupId,
            title = groupTitle,
        )
    }

    private fun mapAccounts(contact: Contact): List<ContactAccount> {
        return contact.rawContacts
            .orEmpty()
            .mapNotNull { rawContact -> mapContactAccount(rawContact) }
            .distinct()
    }

    private fun mapContactAccount(rawContact: RawContact): ContactAccount? {
        val accountType = rawContact.getAccountType(context)
            ?.takeIf { type -> type.areContactsWritable() }
        val name = rawContact.accountName?.trimmedOrNull()
            ?: accountType?.getDisplayLabel(context)?.toString()?.trimmedOrNull()

        return when {
            accountType != null && name != null -> ContactAccount(
                name = name,
                iconData = accountIconData(accountType),
            )

            else -> null
        }
    }

    private fun accountIconData(accountType: AccountType): AccountIconData {
        return AccountIconData(
            titleRes = accountType.titleRes,
            iconRes = accountType.iconRes,
            syncAdapterPackageName = accountType.syncAdapterPackageName,
            applyGrayTint = accountType is FallbackAccountType || accountType is SimAccountType,
        )
    }

    private fun mapDisplayNameSource(displayNameSource: Int): ContactDisplayNameSource {
        return when (displayNameSource) {
            Sources.EMAIL -> ContactDisplayNameSource.EMAIL
            Sources.PHONE -> ContactDisplayNameSource.PHONE
            Sources.ORGANIZATION -> ContactDisplayNameSource.ORGANIZATION
            Sources.NICKNAME -> ContactDisplayNameSource.NICKNAME
            Sources.STRUCTURED_PHONETIC_NAME -> ContactDisplayNameSource.STRUCTURED_PHONETIC_NAME
            Sources.STRUCTURED_NAME -> ContactDisplayNameSource.STRUCTURED_NAME
            else -> ContactDisplayNameSource.UNDEFINED
        }
    }

    private fun mapPhoto(contact: Contact): ContactPhoto? {
        val bytes = contact.photoBinaryData ?: contact.thumbnailPhotoBinaryData
        if (bytes != null) {
            return ContactPhoto.Bytes(ByteBuffer.wrap(bytes))
        }

        val photoUri = contact.photoUri
        return when {
            photoUri.isNullOrEmpty() -> null
            else -> ContactPhoto.Uri(photoUri)
        }
    }

    private fun mapCapabilities(contact: Contact): ContactCapabilities {
        return ContactCapabilities(
            isDirectoryEntry = contact.isDirectoryEntry,
            isAddableDirectoryContact = DirectoryContactUtil.isDirectoryContact(contact),
            isInvisibleAndAddable = InvisibleContactUtil.isInvisibleAndAddable(contact, context),
            isUserProfile = contact.isUserProfile,
            hasMultipleRawContacts = (contact.rawContacts?.size ?: 0) > 1,
            areAllRawContactsSimAccounts = contact.areAllRawContactsSimAccounts(context),
        )
    }

    private fun mapDataItems(
        contact: Contact,
        excludedMimeTypes: Set<String>,
    ): List<ContactDataItem> {
        return contact.rawContacts.orEmpty()
            .flatMap { rawContact ->
                displayableDataItems(rawContact, excludedMimeTypes)
            }
            .groupBy { dataItem -> dataItem.mimeType }
            .values
            .flatMap { dataItems -> dataItemCollapser.collapse(dataItems) }
            .map { dataItem -> mapDataItem(dataItem) }
    }

    private fun displayableDataItems(
        rawContact: RawContact,
        excludedMimeTypes: Set<String>,
    ): List<DataItem> {
        val rawContactId = rawContact.id ?: return emptyList()
        val accountType = rawContact.getAccountType(context)

        return rawContact.dataItems
            .onEach { dataItem ->
                dataItem.rawContactId = rawContactId
                dataItem.dataKind = resolveDataKind(dataItem, accountType)
            }
            .filter { dataItem ->
                isDisplayable(dataItem, excludedMimeTypes)
            }
    }

    private fun resolveDataKind(
        dataItem: DataItem,
        accountType: AccountType?,
    ): DataKind? {
        val mimeType = dataItem.mimeType ?: return null

        return accountTypeManager.getKindOrFallback(accountType, mimeType)
    }

    private fun isDisplayable(
        dataItem: DataItem,
        excludedMimeTypes: Set<String>,
    ): Boolean {
        val dataKind = dataItem.dataKind ?: return false

        return dataItem.mimeType !in excludedMimeTypes &&
            !dataItem.buildDataString(context, dataKind).isNullOrEmpty()
    }

    private fun mapDataItem(dataItem: DataItem): ContactDataItem {
        return mapCommunicationItem(dataItem)
            ?: mapAboutItem(dataItem)
            ?: mapGeneric(dataItem)
    }

    private fun mapCommunicationItem(dataItem: DataItem): ContactDataItem? {
        return when (dataItem) {
            is PhoneDataItem -> mapPhone(dataItem)
            is SipAddressDataItem -> mapSipAddress(dataItem)
            is EmailDataItem -> mapEmail(dataItem)
            is StructuredPostalDataItem -> mapPostal(dataItem)
            is ImDataItem -> mapIm(dataItem)
            else -> null
        }
    }

    private fun mapAboutItem(dataItem: DataItem): ContactDataItem? {
        return when (dataItem) {
            is OrganizationDataItem -> mapOrganization(dataItem)
            is NicknameDataItem -> mapNickname(dataItem)
            is NoteDataItem -> mapNote(dataItem)
            is WebsiteDataItem -> mapWebsite(dataItem)
            is EventDataItem -> mapEvent(dataItem)
            is RelationDataItem -> mapRelation(dataItem)
            is CustomDataItem -> mapCustom(dataItem)
            is StructuredNameDataItem -> mapStructuredName(dataItem)
            else -> null
        }
    }

    private fun mapPhone(dataItem: PhoneDataItem): ContactDataItem.Phone {
        return ContactDataItem.Phone(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            number = dataItem.number,
            formattedNumber = dataItem.formattedPhoneNumber,
            typeLabel = phoneTypeLabel(dataItem),
            isCarrierVideoCallCapable =
                dataItem.carrierPresence and Phone.CARRIER_PRESENCE_VT_CAPABLE != 0,
            preferredPhoneAccount = preferredPhoneAccount(dataItem),
        )
    }

    private fun mapSipAddress(dataItem: SipAddressDataItem): ContactDataItem.SipAddress {
        return ContactDataItem.SipAddress(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            address = dataItem.sipAddress,
            typeLabel = typeLabel(dataItem, dataItem.label) { type, label ->
                SipAddress.getTypeLabel(context.resources, type, label)
            },
        )
    }

    private fun mapEmail(dataItem: EmailDataItem): ContactDataItem.Email {
        return ContactDataItem.Email(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            address = dataItem.address,
            data = dataItem.data,
            typeLabel = typeLabel(dataItem, dataItem.label) { type, label ->
                Email.getTypeLabel(context.resources, type, label)
            },
        )
    }

    private fun mapPostal(dataItem: StructuredPostalDataItem): ContactDataItem.Postal {
        return ContactDataItem.Postal(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            formattedAddress = dataItem.formattedAddress,
            typeLabel = typeLabel(dataItem, dataItem.label) { type, label ->
                StructuredPostal.getTypeLabel(context.resources, type, label)
            },
        )
    }

    private fun mapIm(dataItem: ImDataItem): ContactDataItem.Im {
        val protocol = imProtocol(dataItem)

        return ContactDataItem.Im(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            data = dataItem.data,
            protocol = protocol,
            customProtocol = dataItem.customProtocol,
            protocolLabel = Im.getProtocolLabel(
                context.resources,
                protocol,
                dataItem.customProtocol,
            ).toString(),
        )
    }

    private fun imProtocol(dataItem: ImDataItem): Int {
        return when {
            !dataItem.isProtocolValid -> Im.PROTOCOL_CUSTOM
            dataItem.isCreatedFromEmail -> Im.PROTOCOL_GOOGLE_TALK
            else -> dataItem.protocol ?: Im.PROTOCOL_CUSTOM
        }
    }

    private fun mapOrganization(dataItem: OrganizationDataItem): ContactDataItem.Organization {
        return ContactDataItem.Organization(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            formattedCompany = ContactDisplayUtils.getFormattedCompanyString(
                context,
                dataItem,
                false,
            ),
            company = dataItem.company,
            department = dataItem.department,
            title = dataItem.title,
        )
    }

    private fun preferredPhoneAccount(dataItem: PhoneDataItem): PhoneAccountId? {
        val componentName = dataItem.preferredPhoneAccountComponentName
        val id = dataItem.preferredPhoneAccountId

        return when {
            componentName != null && id != null -> PhoneAccountId(
                componentName = componentName,
                id = id,
            )

            else -> null
        }
    }

    private fun mapNickname(dataItem: NicknameDataItem): ContactDataItem.Nickname {
        return ContactDataItem.Nickname(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            name = dataItem.name,
        )
    }

    private fun mapNote(dataItem: NoteDataItem): ContactDataItem.Note {
        return ContactDataItem.Note(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            note = dataItem.note,
        )
    }

    private fun mapWebsite(dataItem: WebsiteDataItem): ContactDataItem.Website {
        return ContactDataItem.Website(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            url = dataItem.url,
        )
    }

    private fun mapEvent(dataItem: EventDataItem): ContactDataItem.Event {
        val dataString = displayString(dataItem)
        val eventType = dataItem.contentValues.getAsInteger(Event.TYPE)

        return ContactDataItem.Event(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = dataString,
            formattedDate = DateUtils.formatDate(context, dataString),
            typeLabel = typeLabel(dataItem, dataItem.label) { type, label ->
                Event.getTypeLabel(context.resources, type, label)
            },
            isRecurringAnnually =
                eventType == Event.TYPE_ANNIVERSARY || eventType == Event.TYPE_BIRTHDAY,
            isBirthday = eventType == Event.TYPE_BIRTHDAY,
        )
    }

    private fun mapRelation(dataItem: RelationDataItem): ContactDataItem.Relation {
        return ContactDataItem.Relation(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            name = dataItem.name,
            typeLabel = typeLabel(dataItem, dataItem.label) { type, label ->
                Relation.getTypeLabel(context.resources, type, label)
            },
        )
    }

    private fun mapCustom(dataItem: CustomDataItem): ContactDataItem.Custom {
        return ContactDataItem.Custom(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            summary = dataItem.summary,
            content = dataItem.content,
        )
    }

    private fun mapStructuredName(
        dataItem: StructuredNameDataItem,
    ): ContactDataItem.StructuredName {
        return ContactDataItem.StructuredName(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
        )
    }

    private fun mapGeneric(dataItem: DataItem): ContactDataItem.Generic {
        return ContactDataItem.Generic(
            id = dataItem.id,
            rawContactId = dataItem.rawContactId,
            mimeType = dataItem.mimeType,
            isPrimary = dataItem.isPrimary,
            isSuperPrimary = dataItem.isSuperPrimary,
            displayString = displayString(dataItem),
            typeColumn = dataItem.dataKind.typeColumn,
        )
    }

    private fun displayString(dataItem: DataItem): String? {
        return dataItem.buildDataStringForDisplay(context, dataItem.dataKind)
    }

    private fun phoneTypeLabel(dataItem: PhoneDataItem): String? {
        val dataKind = dataItem.dataKind
        if (!dataItem.hasKindTypeColumn(dataKind)) {
            return null
        }

        val type = dataItem.getKindTypeColumn(dataKind)
        val label = dataItem.label

        return when {
            type == Phone.TYPE_CUSTOM && label.isNullOrEmpty() -> ""
            else -> Phone.getTypeLabel(context.resources, type, label).toString()
        }
    }

    private fun typeLabel(
        dataItem: DataItem,
        label: String?,
        resolve: (Int, String?) -> CharSequence,
    ): String? {
        val dataKind = dataItem.dataKind
        if (!dataItem.hasKindTypeColumn(dataKind)) {
            return null
        }

        return resolve(dataItem.getKindTypeColumn(dataKind), label).toString()
    }
}
