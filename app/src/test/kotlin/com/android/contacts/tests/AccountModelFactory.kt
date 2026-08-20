package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountModel

internal object AccountModelFactory {
    fun build(
        name: String = "Account",
        type: String? = null,
        dataSet: String? = null,
    ) = AccountModel(
        name = name,
        type = type,
        dataSet = dataSet,
    )
}
