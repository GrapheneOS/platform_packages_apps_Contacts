package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel

internal object AccountDisplayModelFactory {
    fun build(
        name: String = "Account",
        type: String? = null,
        iconData: AccountIconData? = null,
        isDeviceAccount: Boolean = true,
        areContactsWritable: Boolean = true,
        account: AccountModel = AccountModelFactory.build(name = name, type = type),
    ) = AccountDisplayModel(
        account = account,
        name = name,
        type = type,
        iconData = iconData,
        isDeviceAccount = isDeviceAccount,
        areContactsWritable = areContactsWritable,
    )
}
