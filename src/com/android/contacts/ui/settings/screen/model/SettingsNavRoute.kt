package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsNavRoute {

    val key: String
    val depth: Int

    data object Main : SettingsNavRoute {
        override val key: String = "main"
        override val depth: Int = 0
    }

    data object About : SettingsNavRoute {
        override val key: String = "about"
        override val depth: Int = 1
    }
}
