package com.android.contacts.domain.contacts.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface ContactSelectionFilter {

    data class Contacts(
        val account: AccountModel? = null,
        val includesDirectories: Boolean = false,
        val excludeIds: Set<Long> = emptySet(),
    ) : ContactSelectionFilter

    data class JoinCandidates(
        val targetContactId: Long,
    ) : ContactSelectionFilter

    data class PhoneNumbers(
        val callableOnly: Boolean = false,
        val restrictToContactIds: Set<Long>? = null,
    ) : ContactSelectionFilter

    data class Emails(
        val restrictToContactIds: Set<Long>? = null,
    ) : ContactSelectionFilter

    data object PostalAddresses : ContactSelectionFilter

    sealed interface Legacy : ContactSelectionFilter {
        data object People : Legacy
        data object Phones : Legacy
        data object PostalAddresses : Legacy
    }
}
