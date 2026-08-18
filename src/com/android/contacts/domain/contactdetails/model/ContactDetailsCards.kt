package com.android.contacts.domain.contactdetails.model

internal data class ContactDetailsCards(
    val contactCard: List<ContactEntryGroup>,
    val notes: List<ContactEntryGroup>,
    val headerNickname: String?,
    val headerOrganizationParts: List<String>,
    val groups: List<String>,
)
