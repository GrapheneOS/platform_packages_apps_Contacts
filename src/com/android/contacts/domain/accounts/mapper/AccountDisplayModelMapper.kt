package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.model.account.AccountInfo
import javax.inject.Inject

internal interface AccountDisplayModelMapper {
    fun map(accountInfo: AccountInfo): AccountDisplayModel
}

internal class AccountDisplayModelMapperImpl @Inject constructor(
    private val accountModelMapper: AccountModelMapper,
) : AccountDisplayModelMapper {
    override fun map(accountInfo: AccountInfo): AccountDisplayModel {
        val account = accountModelMapper.map(accountInfo.account)
        return AccountDisplayModel(
            account = account,
            name = accountInfo.nameLabel?.toString(),
            type = accountInfo.typeLabel?.toString(),
            icon = accountInfo.icon,
            isDeviceAccount = accountInfo.isDeviceAccount,
        )
    }
}
