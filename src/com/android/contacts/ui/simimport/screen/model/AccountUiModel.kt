package com.android.contacts.ui.simimport.screen.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel

@Immutable
internal data class AccountUiModel(
    val name: String?,
    val type: String? = null,
    val icon: Drawable? = null,
    private val accountName: String? = null,
    private val accountType: String? = null,
    private val accountDataSet: String? = null,
) {
    constructor(accountDisplayModel: AccountDisplayModel) : this(
        name = accountDisplayModel.name,
        type = accountDisplayModel.type,
        icon = accountDisplayModel.icon,
        accountName = accountDisplayModel.account.name,
        accountType = accountDisplayModel.account.type,
        accountDataSet = accountDisplayModel.account.dataSet,
    )

    val account
        get() = AccountModel(
            name = accountName,
            type = accountType,
            dataSet = accountDataSet,
        )
}
