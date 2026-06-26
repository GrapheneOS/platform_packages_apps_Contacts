package com.android.contacts.tests

import android.graphics.drawable.Drawable
import com.android.contacts.model.account.AccountDisplayInfo
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import kotlin.random.Random

object AccountInfoFactory {
    fun build(
        name: String = "Account Name ${Random.nextInt().toString().take(4)}",
        type: String? = null,
        dataSet: String? = null,
        icon: Drawable? = null,
    ) = AccountInfo(
        AccountDisplayInfo(
            AccountWithDataSet(name, type, dataSet),
            name,
            type,
            icon,
            true,
        ),
        null
    )
}
