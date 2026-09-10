package com.android.contacts.domain.contactdetails.model

import com.android.contacts.data.contactdetails.model.ContactGroup

internal data class ContactDetailsCards(
    val contactCard: List<ContactEntryGroup>,
    val connectedApps: List<ContactConnectedApp>,
    val notes: List<ContactEntryGroup>,
    val headerNicknames: List<String>,
    val headerOrganizations: List<List<String>>,
    val groups: List<ContactGroup>,
)
