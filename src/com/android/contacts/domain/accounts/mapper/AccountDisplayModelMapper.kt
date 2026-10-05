package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.model.account.AccountInfo
import javax.inject.Inject

internal interface AccountDisplayModelMapper {
    fun map(accountInfo: AccountInfo): AccountDisplayModel
}

internal class AccountDisplayModelMapperImpl @Inject constructor(
    private val accountModelMapper: AccountModelMapper,
    private val accountIconDataMapper: AccountIconDataMapper,
) : AccountDisplayModelMapper {
    override fun map(accountInfo: AccountInfo): AccountDisplayModel {
        val account = accountModelMapper.map(accountInfo.account)
        val iconData = accountInfo.type?.let(accountIconDataMapper::map)
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
