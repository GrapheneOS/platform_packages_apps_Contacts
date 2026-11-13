package com.android.contacts.domain.contacts.model

internal data class ContactDirectoryResult(
    val directory: ContactDirectory,
    val contacts: List<ContactItem> = emptyList(),
    val isLoading: Boolean = false,
)
