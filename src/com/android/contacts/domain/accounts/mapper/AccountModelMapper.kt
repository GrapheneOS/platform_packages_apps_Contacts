package com.android.contacts.domain.accounts.mapper

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.account.AccountWithDataSet
import javax.inject.Inject

internal interface AccountModelMapper {
    fun map(accountWithDataSet: AccountWithDataSet): AccountModel
    fun map(accountModel: AccountModel): AccountWithDataSet
}

internal class AccountModelMapperImpl @Inject constructor() : AccountModelMapper {
    override fun map(accountWithDataSet: AccountWithDataSet): AccountModel {
        return AccountModel(
            name = accountWithDataSet.name,
            type = accountWithDataSet.type,
            dataSet = accountWithDataSet.dataSet,
        )
    }

    override fun map(accountModel: AccountModel): AccountWithDataSet {
        return AccountWithDataSet(
            accountModel.name,
            accountModel.type,
            accountModel.dataSet,
        )
    }
}
