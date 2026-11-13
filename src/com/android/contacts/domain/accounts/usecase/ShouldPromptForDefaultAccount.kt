package com.android.contacts.domain.accounts.usecase

import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.preference.ContactsPreferences
import javax.inject.Inject

internal fun interface ShouldPromptForDefaultAccount {
    operator fun invoke(accounts: List<AccountModel>): Boolean
}

internal class ShouldPromptForDefaultAccountImpl @Inject constructor(
    private val contactsPreferences: ContactsPreferences,
    private val accountModelMapper: AccountModelMapper,
) : ShouldPromptForDefaultAccount {
    override operator fun invoke(accounts: List<AccountModel>): Boolean {
        return contactsPreferences.shouldShowAccountChangedNotification(
            accounts.map(accountModelMapper::map),
        )
    }
}
