package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.FallbackAccountType
import com.android.contacts.model.account.SimAccountType
import javax.inject.Inject

internal interface AccountDisplayModelMapper {
    fun map(accountInfo: AccountInfo): AccountDisplayModel
}

internal class AccountDisplayModelMapperImpl @Inject constructor(
    private val accountModelMapper: AccountModelMapper,
) : AccountDisplayModelMapper {
    override fun map(accountInfo: AccountInfo): AccountDisplayModel {
        val account = accountModelMapper.map(accountInfo.account)
        val iconData = accountInfo.type?.let {
            AccountIconData(
                titleRes = accountInfo.type.titleRes,
                iconRes = accountInfo.type.iconRes,
                syncAdapterPackageName = accountInfo.type.syncAdapterPackageName,
                applyGrayTint = accountInfo.type is FallbackAccountType ||
                    accountInfo.type is SimAccountType,
            )
        }

        return AccountDisplayModel(
            account = account,
            name = accountInfo.nameLabel?.toString(),
            type = accountInfo.typeLabel?.toString(),
            iconData = iconData,
            isDeviceAccount = accountInfo.isDeviceAccount,
        )
    }
}
