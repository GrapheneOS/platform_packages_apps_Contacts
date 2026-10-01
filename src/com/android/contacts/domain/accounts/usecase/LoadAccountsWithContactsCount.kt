package com.android.contacts.domain.accounts.usecase

import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal fun interface LoadAccountsWithContactsCount {
    operator fun invoke(
        filter: AccountFilter?,
    ): Flow<Map<AccountDisplayModel, Int>>
}

internal class LoadAccountsWithContactsCountImpl @Inject constructor(
    private val loadAccounts: LoadAccounts,
    private val contactsRepository: ContactsRepository,
) : LoadAccountsWithContactsCount {

    override operator fun invoke(
        filter: AccountFilter?,
    ): Flow<Map<AccountDisplayModel, Int>> {
        return combine(
            loadAccounts(filter),
            contactsRepository.getContactsCount(),
        ) { accounts, counts ->
            val counts = counts.orEmpty()
            accounts.associateWith { account ->
                counts[account.account] ?: 0
            }
        }
    }
}
