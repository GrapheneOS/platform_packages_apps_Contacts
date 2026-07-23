package com.android.contacts.ui.interactions.account

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.R
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.ui.interactions.account.screen.SelectAccountViewModel
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountEffect as Effect
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectAccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun titleId_onSavedStateHandle_isSet() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val titleId = R.string.select_account_dialog_title
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(SelectAccountViewModel.KEY_TITLE_RES_ID to titleId),
                ),
            )

            advanceUntilIdle()
            assertEquals(titleId, viewModel.uiState.value.titleId)
        }

    @Test
    fun accounts_onStart_areLoaded() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account)) },
            )

            advanceUntilIdle()
            with(viewModel.uiState.value) {
                assertEquals(listOf(account.account), accounts?.map { it.account })
            }
        }

    @Test
    fun close_onAccountSelected_isEmittedWithAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account)) },
            )

            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onAction(
                    Action.AccountSelected(viewModel.uiState.value.accounts!!.first())
                )
                advanceUntilIdle()
                assertEquals(Effect.Close(account.account), awaitItem())
            }
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        loadAccounts: LoadAccounts = { emptyFlow() },
    ) = SelectAccountViewModel(
        savedStateHandle = savedStateHandle,
        loadAccounts = loadAccounts,
    )
}
