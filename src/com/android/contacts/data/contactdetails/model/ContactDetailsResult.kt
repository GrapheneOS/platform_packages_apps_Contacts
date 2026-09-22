package com.android.contacts.data.contactdetails.model

internal sealed interface ContactDetailsResult {

    data class Loaded(
        val details: ContactDetails,
        val source: LoadedContact,
    ) : ContactDetailsResult

    data object NotFound : ContactDetailsResult

    data object Error : ContactDetailsResult
}
