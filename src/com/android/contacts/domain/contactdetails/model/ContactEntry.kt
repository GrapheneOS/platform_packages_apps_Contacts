package com.android.contacts.domain.contactdetails.model

internal data class ContactEntry(
    val id: Long,
    val mimeType: String?,
    val kind: ContactEntryKind,
    val isSuperPrimary: Boolean,
    val isDefault: Boolean,
    val header: ContactEntryText?,
    val subHeader: ContactEntryText?,
    val text: String?,
    val copyText: String?,
    val copyLabel: ContactEntryText?,
    val actions: ContactEntryActions,
)
