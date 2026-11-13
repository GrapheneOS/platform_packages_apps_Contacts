package com.android.contacts.domain.contacts.model

internal data class GetContactsForSelectionResult(
    val contacts: List<ContactItem> = emptyList(),
    val suggestions: List<ContactItem> = emptyList(),
    val directories: List<ContactDirectoryResult> = emptyList(),
)
