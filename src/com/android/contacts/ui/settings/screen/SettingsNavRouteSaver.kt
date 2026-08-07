package com.android.contacts.ui.settings.screen

import androidx.compose.runtime.saveable.Saver
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute

private const val MAIN_ROUTE = "main"
private const val ABOUT_ROUTE = "about"

internal val SettingsNavRouteSaver: Saver<SettingsNavRoute, String> = Saver(
    save = { route ->
        when (route) {
            SettingsNavRoute.Main -> MAIN_ROUTE
            SettingsNavRoute.About -> ABOUT_ROUTE
        }
    },
    restore = { savedRoute ->
        when (savedRoute) {
            ABOUT_ROUTE -> SettingsNavRoute.About
            else -> SettingsNavRoute.Main
        }
    },
)
