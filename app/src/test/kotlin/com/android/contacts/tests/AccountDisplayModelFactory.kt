package com.android.contacts.tests

import android.graphics.drawable.Drawable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import kotlin.random.Random

internal object AccountDisplayModelFactory {
    fun build(
        name: String = "Account ${Random.nextInt().toString().take(4)}",
        type: String? = null,
        dataSet: String? = null,
        icon: Drawable? = null,
        isDeviceAccount: Boolean = true,
    ) = AccountDisplayModel(
        name = name,
        type = type,
        dataSet = dataSet,
        icon = icon,
        isDeviceAccount = isDeviceAccount,
    )
}
