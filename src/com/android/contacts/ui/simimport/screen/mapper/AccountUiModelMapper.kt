package com.android.contacts.ui.simimport.screen.mapper

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import javax.inject.Inject

internal fun interface AccountUiModelMapper {
    fun map(accountDisplayModel: AccountDisplayModel): AccountUiModel
}

internal class AccountUiModelMapperImpl @Inject constructor() : AccountUiModelMapper {
    override fun map(accountDisplayModel: AccountDisplayModel): AccountUiModel {
        return AccountUiModel(
            name = accountDisplayModel.name,
            type = accountDisplayModel.type,
            icon = accountDisplayModel.icon,
            accountName = accountDisplayModel.account.name,
            accountType = accountDisplayModel.account.type,
            accountDataSet = accountDisplayModel.account.dataSet,
        )
    }
}
