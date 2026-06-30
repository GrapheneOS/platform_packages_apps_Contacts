package com.android.contacts.domain.accounts.usecase

import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.preference.ContactsPreferences
import javax.inject.Inject

internal fun interface GetDefaultAccount {
    operator fun invoke(): AccountWithDataSet?
}

internal class GetDefaultAccountImpl @Inject constructor(
    private val contactsPreferences: ContactsPreferences,
) : GetDefaultAccount {
    override operator fun invoke(): AccountWithDataSet? = contactsPreferences.defaultAccount
}
