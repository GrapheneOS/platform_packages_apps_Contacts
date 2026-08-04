package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountModel
import kotlin.random.Random

internal object AccountModelFactory {
    fun build(
        name: String = "Account ${Random.nextInt().toString().take(4)}",
        type: String? = null,
        dataSet: String? = null,
    ) = AccountModel(
        name = name,
        type = type,
        dataSet = dataSet,
    )
}
