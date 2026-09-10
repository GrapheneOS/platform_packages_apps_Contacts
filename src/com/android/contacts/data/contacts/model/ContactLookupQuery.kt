package com.android.contacts.data.contacts.model

internal sealed interface ContactLookupQuery {
    data class Email(
        val value: String,
    ) : ContactLookupQuery

    data class Phone(
        val value: String,
    ) : ContactLookupQuery
}
