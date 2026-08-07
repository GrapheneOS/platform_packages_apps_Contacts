package com.android.contacts.ui.settings.screen

import androidx.compose.runtime.saveable.SaverScope
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SettingsNavRouteSaverTest {

    private val saverScope = SaverScope { true }

    @Test
    fun mainRoute_survivesSaveAndRestore() {
        assertEquals(SettingsNavRoute.Main, restore(SettingsNavRoute.Main))
    }

    @Test
    fun aboutRoute_survivesSaveAndRestore() {
        assertEquals(SettingsNavRoute.About, restore(SettingsNavRoute.About))
    }

    @Test
    fun unknownSavedValue_restoresTheMainRoute() {
        assertEquals(SettingsNavRoute.Main, SettingsNavRouteSaver.restore("nonsense"))
    }

    private fun restore(route: SettingsNavRoute): SettingsNavRoute? {
        val saved = with(SettingsNavRouteSaver) { saverScope.save(route) }

        return saved?.let(SettingsNavRouteSaver::restore)
    }
}
