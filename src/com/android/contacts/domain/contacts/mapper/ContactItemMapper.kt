package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.DirectoryContactColumn
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import javax.inject.Inject

internal interface ContactItemMapper {
    fun map(column: ContactColumn, displayOrder: DisplayOrder): ContactItem
    fun map(column: ContactDataColumn, displayOrder: DisplayOrder): ContactItem
    fun map(column: DirectoryContactColumn, displayOrder: DisplayOrder): ContactItem
    fun map(column: LegacyContactColumn): ContactItem
    fun map(details: ContactDetails, displayOrder: DisplayOrder): ContactItem
}

internal class ContactItemMapperImpl @Inject constructor() : ContactItemMapper {

    override fun map(column: ContactColumn, displayOrder: DisplayOrder): ContactItem {
        return ContactItem(
            key = ContactItemKey.Local(column.id),
            name = displayName(
                displayOrder = displayOrder,
                primary = column.displayNamePrimary,
                alternative = column.displayNameAlternative,
            ),
            contactPhoto = column.photo,
            sectionLabel = column.sectionLabel,
            isFavorite = column.isStarred,
        )
    }

    override fun map(column: ContactDataColumn, displayOrder: DisplayOrder): ContactItem {
        return ContactItem(
            key = ContactItemKey.Local(column.id),
            name = displayName(
                displayOrder = displayOrder,
                primary = column.displayNamePrimary,
                alternative = column.displayNameAlternative,
            ),
            contactPhoto = column.photo,
            field = field(
                value = column.value,
                kind = column.kind.toFieldKind(),
                type = column.type,
                label = column.label,
            ),
            sectionLabel = column.sectionLabel,
            isFavorite = column.isStarred,
        )
    }

    override fun map(column: DirectoryContactColumn, displayOrder: DisplayOrder): ContactItem {
        return ContactItem(
            key = ContactItemKey.Directory(
                directoryId = column.directoryId,
                id = column.id,
                lookupKey = column.lookupKey,
            ),
            name = displayName(
                displayOrder = displayOrder,
                primary = column.displayNamePrimary,
                alternative = column.displayNameAlternative,
            ),
            contactPhoto = column.photo,
        )
    }

    override fun map(column: LegacyContactColumn): ContactItem {
        return ContactItem(
            key = ContactItemKey.Local(column.id),
            name = column.displayName.orEmpty(),
            field = field(
                value = column.value,
                kind = column.kind.toFieldKind(),
                type = column.type,
                label = column.label,
            ),
            isFavorite = column.isStarred,
        )
    }

    override fun map(details: ContactDetails, displayOrder: DisplayOrder): ContactItem {
        return ContactItem(
            key = ContactItemKey.Local(details.contactId),
            name = displayName(
                displayOrder = displayOrder,
                primary = details.displayName,
                alternative = details.alternativeDisplayName,
            ),
            contactPhoto = details.photo,
            isFavorite = details.isStarred,
        )
    }

    private fun displayName(
        displayOrder: DisplayOrder,
        primary: String?,
        alternative: String?,
    ): String {
        val preferred = when (displayOrder) {
            DisplayOrder.GIVEN_NAME_FIRST -> primary
            DisplayOrder.FAMILY_NAME_FIRST -> alternative
        }
        return preferred?.takeIf { it.isNotBlank() } ?: primary.orEmpty()
    }

    private fun field(
        value: String?,
        kind: ContactItem.FieldKind,
        type: Int?,
        label: String?,
    ): ContactItem.Field? {
        return value
            ?.takeIf { it.isNotBlank() }
            ?.let { ContactItem.Field(value = it, kind = kind, type = type, label = label) }
    }

    private fun ContactDataKind.toFieldKind(): ContactItem.FieldKind {
        return when (this) {
            ContactDataKind.Email -> ContactItem.FieldKind.EMAIL
            is ContactDataKind.Phone -> ContactItem.FieldKind.PHONE
            ContactDataKind.PostalAddress -> ContactItem.FieldKind.POSTAL_ADDRESS
        }
    }

    private fun LegacyContactsKind.toFieldKind(): ContactItem.FieldKind {
        return when (this) {
            LegacyContactsKind.PEOPLE -> ContactItem.FieldKind.UNSPECIFIED
            LegacyContactsKind.PHONES -> ContactItem.FieldKind.PHONE
            LegacyContactsKind.POSTAL_ADDRESSES -> ContactItem.FieldKind.POSTAL_ADDRESS
        }
    }
}
