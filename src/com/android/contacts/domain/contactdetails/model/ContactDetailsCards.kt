package com.android.contacts.domain.contactdetails.model

import com.android.contacts.data.contactdetails.model.ContactGroup

internal data class ContactDetailsCards(
    val contactCard: List<ContactEntryGroup>,
    val connectedApps: List<ContactConnectedApp>,
    val notes: List<ContactEntryGroup>,
    val headerNickname: String?,
    val headerOrganizationParts: List<String>,
    val groups: List<ContactGroup>,
)
