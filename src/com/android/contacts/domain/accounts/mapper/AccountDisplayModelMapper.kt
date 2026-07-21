package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.account.AccountInfo
import javax.inject.Inject

internal interface AccountDisplayModelMapper {
    fun map(accountInfo: AccountInfo): AccountDisplayModel
}

internal class AccountDisplayModelMapperImpl @Inject constructor() : AccountDisplayModelMapper {
    override fun map(accountInfo: AccountInfo): AccountDisplayModel {
        return AccountDisplayModel(
            name = accountInfo.account.name,
            type = accountInfo.account.type,
            dataSet = accountInfo.account.dataSet,
            icon = accountInfo.icon,
            isDeviceAccount = accountInfo.isDeviceAccount,
        )
    }
}
