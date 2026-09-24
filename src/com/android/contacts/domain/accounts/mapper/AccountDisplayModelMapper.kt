package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.FallbackAccountType
import com.android.contacts.model.account.SimAccountType
import javax.inject.Inject

internal interface AccountDisplayModelMapper {
    fun map(accountModel: AccountModel): AccountDisplayModel?
    fun map(accountInfo: AccountInfo): AccountDisplayModel
}

internal class AccountDisplayModelMapperImpl @Inject constructor(
    private val accountModelMapper: AccountModelMapper,
    private val accountTypeManager: AccountTypeManager,
) : AccountDisplayModelMapper {

    override fun map(accountModel: AccountModel): AccountDisplayModel? {
        val accountWithDataSet = accountModelMapper.map(accountModel)
        val accountInfo = accountTypeManager.getAccountInfoForAccount(accountWithDataSet)
        return accountInfo?.let(::map)
    }

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
            areContactsWritable = accountInfo.type.areContactsWritable(),
        )
    }
}
