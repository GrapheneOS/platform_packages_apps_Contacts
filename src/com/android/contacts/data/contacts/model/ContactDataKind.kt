package com.android.contacts.data.contacts.model

internal sealed interface ContactDataKind {

    data class Phone(
        val callableOnly: Boolean = false,
    ) : ContactDataKind

    data object Email : ContactDataKind

    data object PostalAddress : ContactDataKind
}
