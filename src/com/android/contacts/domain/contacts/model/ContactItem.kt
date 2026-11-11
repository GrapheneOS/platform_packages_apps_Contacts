package com.android.contacts.domain.contacts.model

import com.android.contacts.data.contactdetails.model.ContactPhoto

internal data class ContactItem(
    val key: ContactItemKey,
    val name: String,
    val contactPhoto: ContactPhoto? = null,
    val field: Field? = null,
    val sectionLabel: String? = null,
    val isFavorite: Boolean = false,
) {
    data class Field(
        val value: String,
        val kind: FieldKind,
        val type: Int? = null,
        val label: String? = null,
    )

    enum class FieldKind {
        UNSPECIFIED,
        PHONE,
        EMAIL,
        POSTAL_ADDRESS,
    }
}
