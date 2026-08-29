package com.android.contacts.domain.contactdetails.model

import com.android.contacts.data.connectedapps.model.ConnectedApp

internal data class ContactConnectedApp(
    val app: ConnectedApp,
    val entries: List<ContactEntry>,
)
