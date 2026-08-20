package com.android.contacts.domain.accounts.mapper

import android.content.Context
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.google.common.base.Predicate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface AccountFilterMapper {
    fun map(accountFilter: AccountFilter?): Predicate<AccountInfo>
}

internal class AccountFilterMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AccountFilterMapper {
    override fun map(accountFilter: AccountFilter?): Predicate<AccountInfo> {
        return when (accountFilter) {
            AccountFilter.ALL,
            null,
            ->
                AccountTypeManager.AccountFilter.ALL
            AccountFilter.CONTACTS_WRITABLE ->
                AccountTypeManager.AccountFilter.CONTACTS_WRITABLE
            AccountFilter.CONTACTS_INSERTABLE ->
                AccountTypeManager.insertableFilter(context)
            AccountFilter.DRAWER_DISPLAYABLE ->
                AccountTypeManager.AccountFilter.DRAWER_DISPLAYABLE
            AccountFilter.GROUPS_WRITABLE ->
                AccountTypeManager.AccountFilter.GROUPS_WRITABLE
        }
    }
}
