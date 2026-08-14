package com.android.contacts.ui.settings.screen

import androidx.compose.runtime.saveable.Saver
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute

internal val SettingsNavRouteSaver: Saver<SettingsNavRoute, String> = Saver(
    save = { route -> route.key },
    restore = { savedRoute ->
        when (savedRoute) {
            SettingsNavRoute.About.key -> SettingsNavRoute.About
            else -> SettingsNavRoute.Main
        }
    },
)
