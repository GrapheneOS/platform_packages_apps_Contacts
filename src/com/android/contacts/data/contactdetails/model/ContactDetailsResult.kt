package com.android.contacts.data.contactdetails.model

internal sealed interface ContactDetailsResult {

    data class Loaded(
        val details: ContactDetails,
    ) : ContactDetailsResult

    data object NotFound : ContactDetailsResult

    data object Error : ContactDetailsResult
}
