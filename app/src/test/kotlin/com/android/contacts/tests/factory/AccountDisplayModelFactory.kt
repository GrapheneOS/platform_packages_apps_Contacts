package com.android.contacts.tests.factory

import android.graphics.drawable.Drawable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import kotlin.random.Random

internal object AccountDisplayModelFactory {
    fun build(
        account: AccountModel = AccountModelFactory.build(),
        name: String = account.name ?: "Account ${Random.nextInt().toString().take(4)}",
        type: String? = account.type,
        icon: Drawable? = null,
        isDeviceAccount: Boolean = true,
    ) = AccountDisplayModel(
        account = account,
        name = name,
        type = type,
        icon = icon,
        isDeviceAccount = isDeviceAccount,
    )
}
