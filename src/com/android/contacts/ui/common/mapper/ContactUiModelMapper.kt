package com.android.contacts.ui.common.mapper

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.common.model.ContactUiModel
import javax.inject.Inject

internal interface ContactUiModelMapper {

    fun map(
        contact: ContactItem,
        isSelected: Boolean = false,
    ): ContactUiModel
}

internal class ContactUiModelMapperImpl @Inject constructor(
    private val resources: Resources,
) : ContactUiModelMapper {

    override fun map(
        contact: ContactItem,
        isSelected: Boolean,
    ): ContactUiModel {
        return ContactUiModel(
            key = contact.key,
            name = displayName(contact),
            description = description(contact),
            avatarImage = contact.contactPhoto?.toAvatarImage(),
            isSelected = isSelected,
        )
    }

    private fun displayName(contact: ContactItem): String {
        return contact.name.takeIf { it.isNotBlank() }
            ?: resources.getString(R.string.missing_name)
    }

    private fun description(contact: ContactItem): String? {
        val field = contact.field ?: return null
        // Postal addresses may span across multiple lines
        val value = field.value
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .takeIf { it.any() }
            ?.joinToString(", ")
        val label = fieldLabel(field)
        return when {
            value == null -> null
            label == null -> value
            else -> "$label · $value"
        }
    }

    private fun fieldLabel(field: ContactItem.Field): String? {
        val type = field.type ?: return null
        return when (field.kind) {
            ContactItem.FieldKind.PHONE ->
                Phone.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.EMAIL ->
                Email.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.POSTAL_ADDRESS ->
                StructuredPostal.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.UNSPECIFIED ->
                null
        }
            ?.toString()
            ?.takeIf { it.isNotBlank() }
    }

    private fun ContactPhoto.toAvatarImage(): ContactAvatarImage {
        return when (this) {
            is ContactPhoto.Bytes -> ContactAvatarImage.Bytes(value)
            is ContactPhoto.Uri -> ContactAvatarImage.Uri(value)
        }
    }
}
