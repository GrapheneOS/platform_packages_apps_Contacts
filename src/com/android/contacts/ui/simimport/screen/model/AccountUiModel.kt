package com.android.contacts.ui.simimport.screen.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel

@Immutable
internal data class AccountUiModel(
    val name: String?,
    val type: String? = null,
    val dataSet: String? = null,
    val icon: Drawable? = null,
) {
    constructor(accountDisplayModel: AccountDisplayModel) : this(
        name = accountDisplayModel.name,
        type = accountDisplayModel.type,
        dataSet = accountDisplayModel.dataSet,
        icon = accountDisplayModel.icon,
    )

    fun isSameAccount(account: AccountModel): Boolean {
        return toModel() == account
    }

    fun toModel(): AccountModel {
        return AccountModel(
            name = name,
            type = type,
            dataSet = dataSet,
        )
    }
}
