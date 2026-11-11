package com.android.contacts.data.contacts.model

import com.android.contacts.data.contactdetails.model.ContactPhoto

internal data class ContactColumn(
    val id: Long,
    val displayNamePrimary: String? = null,
    val displayNameAlternative: String? = null,
    val photo: ContactPhoto? = null,
    val sectionLabel: String? = null,
    val isStarred: Boolean = false,
)
