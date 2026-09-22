package com.android.contacts.domain.contactdetails.model

internal data class ContactEntryActions(
    val primaryAction: ContactEntryAction? = null,
    val alternateAction: ContactEntryAction? = null,
    val enhancedCallAction: ContactEntryAction? = null,
    val editBeforeCallAction: ContactEntryAction? = null,
)
