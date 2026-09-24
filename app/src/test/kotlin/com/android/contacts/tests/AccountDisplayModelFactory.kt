package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel

internal object AccountDisplayModelFactory {
    fun build(
        name: String = "Account",
        type: String? = null,
        account: AccountModel = AccountModelFactory.build(name = name, type = type),
        iconData: AccountIconData? = null,
        isDeviceAccount: Boolean = true,
        areContactsWritable: Boolean = true,
    ) = AccountDisplayModel(
        account = account,
        name = name,
        type = type,
        iconData = iconData,
        isDeviceAccount = isDeviceAccount,
        areContactsWritable = areContactsWritable,
    )
}
