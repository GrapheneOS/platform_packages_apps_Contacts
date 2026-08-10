package com.android.contacts.domain.contactdetails.model

internal data class ContactDetailsCards(
    val contactCard: List<ContactEntryGroup>,
    val aboutCard: List<ContactEntryGroup>,
    val aboutCardGivenName: String?,
)
