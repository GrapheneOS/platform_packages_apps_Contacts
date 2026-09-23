package com.android.contacts.ui.interactions.account

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.ui.interactions.account.screen.SelectAccountViewModel
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountEffect as Effect
import com.android.contacts.ui.simimport.screen.mapper.AccountUiModelMapperImpl
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

    // Not mocking mappers since they hold no logic
    private val accountUiModelMapper = AccountUiModelMapperImpl()

    @Test
    fun titleId_onSavedStateHandle_isSet() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val titleId = R.string.select_account_dialog_title
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(SelectAccountViewModel.KEY_TITLE_RES_ID to titleId),
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(titleId, expectMostRecentItem().titleId)
            }
        }

    @Test
    fun accounts_onStart_areLoaded() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account)) },
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    listOf(account.account),
                    expectMostRecentItem().accounts?.map { it.account },
                )
            }
        }

    @Test
    fun close_onAccountSelected_isEmittedWithAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account)) },
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.effects.test {
                viewModel.onAction(Action.AccountSelected(account.toUiModel()))
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
        accountUiModelMapper = accountUiModelMapper,
    )

    private fun AccountDisplayModel.toUiModel() = accountUiModelMapper.map(this)
}
