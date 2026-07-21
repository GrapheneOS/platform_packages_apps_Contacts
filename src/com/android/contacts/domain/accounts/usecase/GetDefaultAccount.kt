package com.android.contacts.domain.accounts.usecase

import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.preference.ContactsPreferences
import javax.inject.Inject

internal fun interface GetDefaultAccount {
    operator fun invoke(): AccountModel?
}

internal class GetDefaultAccountImpl @Inject constructor(
    private val contactsPreferences: ContactsPreferences,
    private val accountModelMapper: AccountModelMapper,
) : GetDefaultAccount {
    override operator fun invoke(): AccountModel? {
        return contactsPreferences.defaultAccount
            ?.let(accountModelMapper::map)
    }
}
