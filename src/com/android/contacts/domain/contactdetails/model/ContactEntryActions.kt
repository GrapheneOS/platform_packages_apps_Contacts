package com.android.contacts.domain.contactdetails.model

internal data class ContactEntryActions(
    val primary: ContactEntryAction? = null,
    val alternate: ContactEntryAction? = null,
    val third: ContactEntryAction? = null,
)
