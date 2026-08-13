package com.android.contacts.tests

import android.graphics.drawable.Drawable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import kotlin.random.Random

internal object AccountDisplayModelFactory {
    fun build(
        name: String = "Account",
        type: String? = null,
        icon: Drawable? = null,
        isDeviceAccount: Boolean = true,
    ) = AccountDisplayModel(
        account = AccountModelFactory.build(name = name, type = type),
        name = name,
        type = type,
        icon = icon,
        isDeviceAccount = isDeviceAccount,
    )
}
