package com.android.contacts.domain.contactdetails.model

internal data class ContactEntryGroup(
    val mimeType: String?,
    val entries: List<ContactEntry>,
)
