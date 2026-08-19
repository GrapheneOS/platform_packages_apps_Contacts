package com.android.contacts.tests

import android.graphics.drawable.Drawable
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.simimport.screen.model.AccountUiModel

internal object AccountUiModelFactory {
    fun build(
        account: AccountModel = AccountModelFactory.build(),
        name: String = account.name ?: "Account",
        type: String? = account.type,
        icon: Drawable? = null,
    ) = AccountUiModel(
        name = name,
        type = type,
        icon = icon,
        accountName = account.name,
        accountType = account.type,
        accountDataSet = account.dataSet,
    )
}
