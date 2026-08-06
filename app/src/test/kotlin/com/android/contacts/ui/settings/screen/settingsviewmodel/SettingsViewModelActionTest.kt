package com.android.contacts.ui.settings.screen.settingsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModelActionTest : BaseSettingsViewModelTest() {

    @Test
    fun onAction_whenAccountsClicked_opensAddAccount() =
        assertItemClickEmits(SettingsItemId.ACCOUNTS, Effect.OpenAddAccount)

    @Test
    fun onAction_whenDefaultAccountClicked_opensPicker() =
        assertItemClickEmits(SettingsItemId.DEFAULT_ACCOUNT, Effect.OpenDefaultAccountPicker)

    @Test
    fun onAction_whenContactsFilterClicked_opensFilter() =
        assertItemClickEmits(SettingsItemId.CONTACTS_FILTER, Effect.OpenContactsFilter)

    @Test
    fun onAction_whenImportClicked_showsImportDialog() =
        assertItemClickEmits(SettingsItemId.IMPORT, Effect.ShowImportDialog)

    @Test
    fun onAction_whenExportClicked_showsExportDialog() =
        assertItemClickEmits(SettingsItemId.EXPORT, Effect.ShowExportDialog)

    @Test
    fun onAction_whenBlockedNumbersClicked_opensBlockedNumbers() =
        assertItemClickEmits(SettingsItemId.BLOCKED_NUMBERS, Effect.OpenBlockedNumbers)

    @Test
    fun onAction_whenLicensesClicked_opensLicenses() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.LicensesClicked)

                assertEquals(Effect.OpenLicenses, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onAction_whenMyInfoClickedWithProfile_opensIt() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            profiles.value = ProfileData(hasProfile = true, contactId = 7L)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(Action.ItemClicked(SettingsItemId.MY_INFO))

                assertEquals(Effect.OpenProfile(contactId = 7L), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onAction_whenMyInfoClickedWithoutProfile_offersToCreateIt() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            profiles.value = ProfileData(hasProfile = false, contactId = 7L)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(Action.ItemClicked(SettingsItemId.MY_INFO))

                assertEquals(Effect.CreateProfile, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onAction_whenDialogRowsAreClicked_emitsNoEffect() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.ItemClicked(SettingsItemId.SORT_ORDER))
                viewModel.onAction(Action.ItemClicked(SettingsItemId.DISPLAY_ORDER))
                viewModel.onAction(Action.ItemClicked(SettingsItemId.PHONETIC_NAME_DISPLAY))
                viewModel.onAction(Action.ItemClicked(SettingsItemId.ABOUT))
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    @Test
    fun onAction_whenSortOrderSelected_storesItAndReloads() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.uiState.test {
                advanceUntilIdle()

                coEvery { getSettingsData() } returns reloadedSettingsData
                viewModel.onAction(Action.SortOrderSelected(SortOrder.FAMILY_NAME_FIRST))
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    displaySettingsRepository.setSortOrder(SortOrder.FAMILY_NAME_FIRST)
                }
                assertEquals(reloadedState, expectMostRecentItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun onAction_whenDisplayOrderSelected_storesIt() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onAction(Action.DisplayOrderSelected(DisplayOrder.FAMILY_NAME_FIRST))
            advanceUntilIdle()

            coVerify(exactly = 1) {
                displaySettingsRepository.setDisplayOrder(DisplayOrder.FAMILY_NAME_FIRST)
            }
        }

    @Test
    fun onAction_whenPhoneticNameDisplaySelected_storesIt() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onAction(
                Action.PhoneticNameDisplaySelected(PhoneticNameDisplay.HIDE_IF_EMPTY),
            )
            advanceUntilIdle()

            coVerify(exactly = 1) {
                displaySettingsRepository.setPhoneticNameDisplay(PhoneticNameDisplay.HIDE_IF_EMPTY)
            }
        }

    private fun assertItemClickEmits(
        id: SettingsItemId,
        expected: Effect,
    ) = runTest(context = mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.ItemClicked(id))

            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
