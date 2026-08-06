package com.android.contacts.ui.settings.screen.settingsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModelStateTest : BaseSettingsViewModelTest() {

    @Test
    fun uiState_startsEmptyAndThenShowsMappedState() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(SettingsUiState(), awaitItem())
                assertEquals(mappedState, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_whenProfileChanges_isRemapped() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val profile = ProfileData(hasProfile = true, contactId = 7L, displayName = "Anna")
            every {
                settingsUiStateMapper.map(settingsData = settingsData, profile = profile)
            } returns reloadedState
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(SettingsUiState(), awaitItem())
                assertEquals(mappedState, awaitItem())

                profiles.value = profile

                assertEquals(reloadedState, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun refreshState_reloadsSettingsData() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(SettingsUiState(), awaitItem())
                assertEquals(mappedState, awaitItem())

                coEvery { getSettingsData() } returns reloadedSettingsData
                viewModel.refreshState()

                assertEquals(reloadedState, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onAction_whenContactsFilterChanged_reloadsSettingsData() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            assertReloadsOn(Action.ContactsFilterChanged)
        }

    @Test
    fun onAction_whenDefaultAccountChanged_reloadsSettingsData() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            assertReloadsOn(Action.DefaultAccountChanged)
        }

    private suspend fun assertReloadsOn(action: Action) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SettingsUiState(), awaitItem())
            assertEquals(mappedState, awaitItem())

            coEvery { getSettingsData() } returns reloadedSettingsData
            viewModel.onAction(action)

            assertEquals(reloadedState, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
