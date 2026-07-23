package com.android.contacts.ui.interactions.importing

import app.cash.turbine.test
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.domain.vcard.usecase.CanImportFromVCard
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.SimCardOptionFactory
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.SimCardFactory
import com.android.contacts.ui.interactions.importing.screen.ImportViewModel
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun isLoading_whenLoadSimCardFinishes_isFalse() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val simCard = SimCardFactory.build()
            val viewModel = createViewModel(
                canImportFromVCard = { true },
                loadSimCards = { flowOf(listOf(simCard)) },
            )

            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun withMultipleAccounts_onVCardClick_openSelectAccountIsEmitted() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                loadAccounts = {
                    flowOf(
                        (1..3).map { AccountDisplayModelFactory.build() }.toImmutableList(),
                    )
                },
            )

            viewModel.effects.test {
                viewModel.onAction(Action.VCardClick)
                advanceUntilIdle()
                assertEquals(Effect.OpenSelectAccount, awaitItem())
            }
        }

    @Test
    fun withOneAccount_onVCardClick_openVCardImport() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account)) },
            )

            viewModel.effects.test {
                viewModel.onAction(Action.VCardClick)
                advanceUntilIdle()
                assertEquals(Effect.OpenVCardImport(account.toModel()), awaitItem())
            }
        }

    @Test
    fun simCards_onLoad_areMappedToOptions() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val simCard = SimCardFactory.build()
            val simOption = SimCardOptionFactory.build()
            val mapper = mockk<SimCardOptionMapper> {
                every { map(any()) } returns simOption
            }
            val viewModel = createViewModel(
                loadSimCards = { flowOf(listOf(simCard)) },
                simCardOptionMapper = mapper,
            )

            advanceUntilIdle()
            verify { mapper.map(eq(simCard)) }
            assertEquals(persistentListOf(simOption), viewModel.uiState.value.simCardOptions)
        }

    private fun createViewModel(
        canImportFromVCard: CanImportFromVCard = { true },
        loadSimCards: LoadSimCards = { emptyFlow() },
        simCardOptionMapper: SimCardOptionMapper = { SimCardOptionFactory.build() },
        loadAccounts: LoadAccounts = { emptyFlow() },
    ) = ImportViewModel(
        canImportFromVCard = canImportFromVCard,
        loadSimCards = loadSimCards,
        simCardOptionMapper = simCardOptionMapper,
        loadAccounts = loadAccounts,
    )
}
