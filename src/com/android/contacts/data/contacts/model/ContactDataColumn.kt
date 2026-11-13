package com.android.contacts.data.contacts.model

import com.android.contacts.data.contactdetails.model.ContactPhoto

internal data class ContactDataColumn(
    val id: Long,
    val kind: ContactDataKind,
    val displayNamePrimary: String? = null,
    val displayNameAlternative: String? = null,
    val photo: ContactPhoto? = null,
    val value: String? = null,
    val type: Int? = null,
    val label: String? = null,
    val sectionLabel: String? = null,
    val isStarred: Boolean = false,
)
