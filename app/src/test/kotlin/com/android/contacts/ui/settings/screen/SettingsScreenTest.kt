package com.android.contacts.ui.settings.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SettingsScreenTest {

    private val effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    private val screenModel = mockk<SettingsScreenModel>(relaxed = true)
    private val effectHandler = mockk<SettingsEffectHandler>(relaxed = true)

    @Before
    fun setUp() {
        every { screenModel.uiState } returns MutableStateFlow(SettingsUiState())
        every { screenModel.effects } returns effects
    }

    @Test
    fun whenSimImportSucceeds_showsHowManyContactsWereImported() = runComposeUiTest {
        setScreenContent()

        effects.tryEmit(Effect.ShowSimImportSuccess(importedCount = 3))
        waitForIdle()

        onNodeWithText("3 SIM contacts imported").assertIsDisplayed()
    }

    @Test
    fun whenASingleContactIsImported_showsTheSingularMessage() = runComposeUiTest {
        setScreenContent()

        effects.tryEmit(Effect.ShowSimImportSuccess(importedCount = 1))
        waitForIdle()

        onNodeWithText("1 SIM contact imported").assertIsDisplayed()
    }

    @Test
    fun whenSimImportFails_showsTheFailureMessage() = runComposeUiTest {
        setScreenContent()

        effects.tryEmit(Effect.ShowSimImportFailure)
        waitForIdle()

        onNodeWithText("Failed to import SIM contacts").assertIsDisplayed()
    }

    @Test
    fun simImportResults_areNotPassedToTheEffectHandler() = runComposeUiTest {
        setScreenContent()

        effects.tryEmit(Effect.ShowSimImportFailure)
        waitForIdle()

        verify(exactly = 0) { effectHandler.handle(any()) }
    }

    @Test
    fun otherEffects_arePassedToTheEffectHandler() = runComposeUiTest {
        setScreenContent()

        effects.tryEmit(Effect.OpenLicenses)
        waitForIdle()

        verify(exactly = 1) { effectHandler.handle(Effect.OpenLicenses) }
    }

    @Test
    fun onResume_refreshesTheState() = runComposeUiTest {
        setScreenContent()

        verify(atLeast = 1) { screenModel.refreshState() }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            SettingsScreen(
                effectHandler = effectHandler,
                onNavigateBack = {},
                screenModel = screenModel,
            )
        }
    }
}
