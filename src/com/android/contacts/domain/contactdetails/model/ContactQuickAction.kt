package com.android.contacts.domain.contactdetails.model

internal data class ContactQuickAction(
    val type: ContactQuickActionType,
    val action: ContactEntryAction?,
)
