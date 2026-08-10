package com.android.contacts.domain.contactdetails.model

internal sealed interface ContactEntryText {

    data class Value(
        val text: String,
    ) : ContactEntryText

    data class Label(
        val label: ContactEntryLabel,
    ) : ContactEntryText
}
