package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.model.account.AccountType
import com.android.contacts.model.account.FallbackAccountType
import com.android.contacts.model.account.SimAccountType
import javax.inject.Inject

internal interface AccountIconDataMapper {
    fun map(accountType: AccountType): AccountIconData
}

internal class AccountIconDataMapperImpl @Inject constructor() : AccountIconDataMapper {
    override fun map(accountType: AccountType): AccountIconData {
        return AccountIconData(
            titleRes = accountType.titleRes,
            iconRes = accountType.iconRes,
            syncAdapterPackageName = accountType.syncAdapterPackageName,
            applyGrayTint = accountType is FallbackAccountType ||
                accountType is SimAccountType,
        )
    }
}
