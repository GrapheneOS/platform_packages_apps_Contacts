package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.simimport.screen.model.AccountUiModel

internal object AccountUiModelFactory {
    fun build(
        account: AccountModel = AccountModelFactory.build(),
        name: String = account.name ?: "Account",
        type: String? = account.type,
        iconData: AccountIconData? = null,
    ) = AccountUiModel(
        account = account,
        name = name,
        type = type,
        iconData = iconData,
    )
}
