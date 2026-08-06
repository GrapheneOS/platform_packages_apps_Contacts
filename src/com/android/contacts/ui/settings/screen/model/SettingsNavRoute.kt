package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsNavRoute {

    val depth: Int

    data object Main : SettingsNavRoute {
        override val depth: Int = 0
    }

    data object About : SettingsNavRoute {
        override val depth: Int = 1
    }
}
