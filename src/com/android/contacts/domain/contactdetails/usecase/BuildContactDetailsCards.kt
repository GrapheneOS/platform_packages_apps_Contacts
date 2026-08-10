package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Identity
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.model.dataitem.CustomDataItem
import javax.inject.Inject

internal fun interface BuildContactDetailsCards {
    operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards
}

internal class BuildContactDetailsCardsImpl @Inject constructor() : BuildContactDetailsCards {

    override operator fun invoke(
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): ContactDetailsCards {
        val groups = details.dataItems
            .groupBy { dataItem -> dataItem.mimeType }
            .mapValues { group -> group.value.sortedWith(WITHIN_MIME_TYPE) }

        return ContactDetailsCards(
            contactCard = buildContactCard(groups, details, prioritizedMimeType),
            aboutCard = withPhoneticName(buildAboutCard(groups, details), details.phoneticName),
            aboutCardGivenName = aboutCardGivenName(groups),
        )
    }

    private fun buildContactCard(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
        prioritizedMimeType: String?,
    ): List<ContactEntryGroup> {
        return groups.keys
            .filterNot { mimeType -> mimeType in ABOUT_CARD_MIME_TYPES }
            .sortedBy { mimeType -> mimeTypeRank(mimeType, prioritizedMimeType) }
            .map { mimeType -> toGroup(mimeType, groups.getValue(mimeType), details) }
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun buildAboutCard(
        groups: Map<String, List<ContactDataItem>>,
        details: ContactDetails,
    ): List<ContactEntryGroup> {
        return ABOUT_CARD_MIME_TYPES
            .filter { mimeType -> groups.containsKey(mimeType) }
            .map { mimeType -> toGroup(mimeType, groups.getValue(mimeType), details) }
            .filterNot { group -> group.entries.isEmpty() }
    }

    private fun withPhoneticName(
        aboutCard: List<ContactEntryGroup>,
        phoneticName: String?,
    ): List<ContactEntryGroup> {
        if (phoneticName.isNullOrEmpty()) {
            return aboutCard
        }

        val group = ContactEntryGroup(
            mimeType = null,
            entries = listOf(phoneticEntry(phoneticName)),
        )

        return aboutCard.toMutableList().apply {
            add(phoneticNameIndex(aboutCard), group)
        }
    }

    private fun phoneticNameIndex(aboutCard: List<ContactEntryGroup>): Int {
        return when (aboutCard.firstOrNull()?.mimeType) {
            Nickname.CONTENT_ITEM_TYPE -> 1
            else -> 0
        }
    }

    private fun phoneticEntry(phoneticName: String): ContactEntry {
        return ContactEntry(
            id = NO_DATA_ID,
            mimeType = null,
            header = ContactEntryText.Label(ContactEntryLabel.PHONETIC_NAME),
            subHeader = phoneticName,
            text = null,
            isSuperPrimary = false,
            copyText = phoneticName,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONETIC_NAME),
        )
    }

    private fun aboutCardGivenName(groups: Map<String, List<ContactDataItem>>): String? {
        return groups[StructuredName.CONTENT_ITEM_TYPE]
            .orEmpty()
            .filterIsInstance<ContactDataItem.StructuredName>()
            .firstOrNull()
            ?.givenName
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
        return ContactEntryGroup(
            mimeType = mimeType,
            entries = dataItems.mapNotNull { dataItem ->
                toEntry(dataItem, details)
            },
        )
    }

    private fun toEntry(
        dataItem: ContactDataItem,
        details: ContactDetails,
    ): ContactEntry? {
        val content = toContent(dataItem, details) ?: return null
        if (content.isEmpty()) {
            return null
        }

        return ContactEntry(
            id = dataItem.id,
            mimeType = dataItem.mimeType,
            header = content.header,
            subHeader = content.subHeader,
            text = content.text,
            isSuperPrimary = dataItem.isSuperPrimary,
            copyText = content.copyText,
            copyLabel = content.copyLabel ?: content.header,
        )
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
            is ContactDataItem.Organization -> organizationContent(dataItem)
            is ContactDataItem.Nickname -> nicknameContent(dataItem, details)
            is ContactDataItem.Note -> noteContent(dataItem)
            is ContactDataItem.Website -> websiteContent(dataItem)
            is ContactDataItem.Event -> eventContent(dataItem)
            is ContactDataItem.Relation -> relationContent(dataItem)
            is ContactDataItem.Custom -> customContent(dataItem)
            is ContactDataItem.Generic -> genericContent(dataItem)
            is ContactDataItem.StructuredName -> null
        }
    }

    private fun phoneContent(dataItem: ContactDataItem.Phone): EntryContent? {
        if (dataItem.number.isNullOrEmpty()) {
            return null
        }

        val header = dataItem.displayString.orEmpty()

        return EntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeLabel,
            copyText = header,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
        )
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
        )
    }

    private fun imContent(dataItem: ContactDataItem.Im): EntryContent {
        if (dataItem.isCustomProtocol) {
            return EntryContent(
                header = ContactEntryText.Label(ContactEntryLabel.IM),
                subHeader = dataItem.protocolLabel,
                text = dataItem.data,
                copyText = dataItem.data,
            )
        }

        return EntryContent(
            header = ContactEntryText.Value(dataItem.protocolLabel.orEmpty()),
            subHeader = dataItem.data,
            copyText = dataItem.data,
        )
    }

    private fun organizationContent(dataItem: ContactDataItem.Organization): EntryContent {
        return EntryContent(
            header = ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
            text = dataItem.formattedCompany,
            copyText = dataItem.formattedCompany,
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
            header = ContactEntryText.Label(ContactEntryLabel.NICKNAME),
            subHeader = dataItem.name,
            copyText = dataItem.name,
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
            header = ContactEntryText.Label(ContactEntryLabel.NOTE),
            subHeader = dataItem.note,
            copyText = dataItem.note,
        )
    }

    private fun websiteContent(dataItem: ContactDataItem.Website): EntryContent {
        return EntryContent(
            header = ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            subHeader = dataItem.url,
            copyText = dataItem.url,
        )
    }

    private fun eventContent(dataItem: ContactDataItem.Event): EntryContent {
        return EntryContent(
            header = ContactEntryText.Label(ContactEntryLabel.EVENT),
            subHeader = dataItem.typeLabel,
            text = dataItem.formattedDate,
            copyText = dataItem.formattedDate,
        )
    }

    private fun relationContent(dataItem: ContactDataItem.Relation): EntryContent {
        return EntryContent(
            header = ContactEntryText.Label(ContactEntryLabel.RELATION),
            subHeader = dataItem.name,
            text = dataItem.typeLabel,
            copyText = dataItem.name,
        )
    }

    private fun customContent(dataItem: ContactDataItem.Custom): EntryContent {
        val summary = dataItem.summary

        return EntryContent(
            header = when {
                summary.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD)
                else -> ContactEntryText.Value(summary)
            },
            subHeader = dataItem.content,
            copyText = dataItem.content,
        )
    }

    private fun genericContent(dataItem: ContactDataItem.Generic): EntryContent {
        val header = dataItem.displayString.orEmpty()

        return EntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeColumn,
            copyText = header,
            copyLabel = ContactEntryText.Value(dataItem.mimeType),
        )
    }

    private data class EntryContent(
        val header: ContactEntryText?,
        val subHeader: String? = null,
        val text: String? = null,
        val copyText: String? = null,
        val copyLabel: ContactEntryText? = null,
    ) {
        fun isEmpty(): Boolean {
            return isHeaderEmpty() && subHeader.isNullOrEmpty() && text.isNullOrEmpty()
        }

        private fun isHeaderEmpty(): Boolean {
            return when (header) {
                null -> true
                is ContactEntryText.Label -> false
                is ContactEntryText.Value -> header.text.isEmpty()
            }
        }
    }

    private companion object {
        const val NOT_FOUND = -1
        const val NO_DATA_ID = -1L
        const val PRIORITIZED_RANK = -1

        val WITHIN_MIME_TYPE = compareByDescending<ContactDataItem> { dataItem ->
            dataItem.isSuperPrimary
        }.thenByDescending { dataItem ->
            dataItem.isPrimary
        }

        val LEADING_MIME_TYPES = listOf(
            Phone.CONTENT_ITEM_TYPE,
            SipAddress.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE,
        )

        val ABOUT_CARD_MIME_TYPES = listOf(
            Nickname.CONTENT_ITEM_TYPE,
            Website.CONTENT_ITEM_TYPE,
            Organization.CONTENT_ITEM_TYPE,
            Event.CONTENT_ITEM_TYPE,
            Relation.CONTENT_ITEM_TYPE,
            Im.CONTENT_ITEM_TYPE,
            GroupMembership.CONTENT_ITEM_TYPE,
            Identity.CONTENT_ITEM_TYPE,
            CustomDataItem.MIMETYPE_CUSTOM_FIELD,
            Note.CONTENT_ITEM_TYPE,
        )
    }
}
