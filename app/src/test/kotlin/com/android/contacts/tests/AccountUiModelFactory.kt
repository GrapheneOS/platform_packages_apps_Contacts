package com.android.contacts.tests

import android.graphics.drawable.Drawable
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import kotlin.random.Random

internal object AccountUiModelFactory {
    fun build(
        name: String = "Account ${Random.nextInt().toString().take(4)}",
        type: String? = null,
        dataSet: String? = null,
        icon: Drawable? = null,
    ) = AccountUiModel(
        name = name,
        type = type,
        dataSet = dataSet,
        icon = icon,
    )
}
