package com.android.contacts.domain.contacts.model

internal data class ContactDirectory(
    val id: Long,
    val displayName: String? = null,
    val packageName: String? = null,
    val typeResourceId: Int? = null,
)
