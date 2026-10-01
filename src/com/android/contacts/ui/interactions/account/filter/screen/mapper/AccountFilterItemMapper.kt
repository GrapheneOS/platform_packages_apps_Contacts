package com.android.contacts.ui.interactions.account.filter.screen.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import javax.inject.Inject

internal fun interface AccountFilterItemMapper {
    fun map(accounts: Map<AccountDisplayModel, Int>): List<AccountFilterItem>
}

internal class AccountFilterItemMapperImpl @Inject constructor() : AccountFilterItemMapper {
    override fun map(accounts: Map<AccountDisplayModel, Int>): List<AccountFilterItem> {
        return buildList {
            add(allItem(accounts.values.sum()))
            addAll(
                accounts.entries.map { (account, contactsCount) ->
                    map(account, contactsCount)
                },
            )
        }
    }

    private fun allItem(contactsCount: Int): AccountFilterItem {
        return AccountFilterItem.All(
            contactsCount = contactsCount,
        )
    }

    private fun map(account: AccountDisplayModel, contactsCount: Int): AccountFilterItem {
        return AccountFilterItem.One(
            name = account.name ?: account.type ?: "",
            description = when {
                account.name != null && account.name != account.type -> account.type
                else -> null
            },
            contactsCount = contactsCount,
            iconData = account.iconData,
            filter = ContactsAccountFilter.One(account.account),
        )
    }
}
