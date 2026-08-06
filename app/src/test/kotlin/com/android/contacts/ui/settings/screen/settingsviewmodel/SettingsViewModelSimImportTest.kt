package com.android.contacts.ui.settings.screen.settingsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.simimport.model.SimImportResult
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModelSimImportTest : BaseSettingsViewModelTest() {

    @Test
    fun simImportSuccess_isReportedAsEffect() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                simImportResults.emit(SimImportResult.Success(importedCount = 3))

                assertEquals(Effect.ShowSimImportSuccess(importedCount = 3), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun simImportFailure_isReportedAsEffect() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.effects.test {
                simImportResults.emit(SimImportResult.Failure)

                assertEquals(Effect.ShowSimImportFailure, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
