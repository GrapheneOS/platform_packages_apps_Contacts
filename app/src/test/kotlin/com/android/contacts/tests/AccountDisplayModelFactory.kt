package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountIconData

internal object AccountDisplayModelFactory {
    fun build(
        name: String = "Account",
        type: String? = null,
        iconData: AccountIconData? = null,
        isDeviceAccount: Boolean = true,
    ) = AccountDisplayModel(
        account = AccountModelFactory.build(name = name, type = type),
        name = name,
        type = type,
        iconData = iconData,
        isDeviceAccount = isDeviceAccount,
    )
}
