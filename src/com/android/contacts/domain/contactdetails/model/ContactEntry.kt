package com.android.contacts.domain.contactdetails.model

internal data class ContactEntry(
    val id: Long,
    val mimeType: String?,
    val isSuperPrimary: Boolean,
    val header: ContactEntryText?,
    val subHeader: String?,
    val text: String?,
    val copyText: String?,
    val copyLabel: ContactEntryText?,
    val actions: ContactEntryActions,
)
