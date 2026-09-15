package com.android.contacts.ui.interactions.importing.screen

import app.cash.turbine.awaitItem
import app.cash.turbine.test
import com.android.contacts.data.settings.model.SettingsAvailability
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.model.SimCard
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.SimCardFactory
import com.android.contacts.tests.factory.SimCardOptionFactory
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsAvailabilityRepo = mockk<SettingsAvailabilityRepository>(relaxed = true)

    @Before
    fun setUp() {
        coEvery { settingsAvailabilityRepo.getSettingsAvailability() } returns SETTINGS_AVAILABILITY
    }

    @Test
    fun isLoading_whileSettingsAvailabilityAndSimCardsAreLoading_isTrue() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val availabilityChannel = Channel<SettingsAvailability>(Channel.BUFFERED)
            coEvery {
                settingsAvailabilityRepo.getSettingsAvailability()
            } coAnswers { availabilityChannel.awaitItem() }
            val loadSimCardsFlow = MutableStateFlow<List<SimCard>?>(null)
            val viewModel = createViewModel(
                loadSimCards = { loadSimCardsFlow.filterNotNull() },
            )

            viewModel.uiState.test {
                assertTrue(awaitItem().isLoading)
                availabilityChannel.send(SETTINGS_AVAILABILITY)
                loadSimCardsFlow.value = listOf(SimCardFactory.build())
                advanceUntilIdle()
                assertFalse(awaitItem().isLoading)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun whenImportIsNotAvailable_setStateValue() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            coEvery {
                settingsAvailabilityRepo.getSettingsAvailability()
            } coAnswers { SETTINGS_AVAILABILITY.copy(isImportFromVCardAvailable = false) }

            val viewModel = createViewModel(
                loadSimCards = { flowOf(emptyList()) },
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(false, expectMostRecentItem().isVCardImportAvailable)
            }
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
                advanceUntilIdle()
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

            viewModel.uiState.test {
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.effects.test {
                viewModel.onAction(Action.VCardClick)
                advanceUntilIdle()
                assertEquals(Effect.OpenVCardImport(account.account), awaitItem())
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

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(persistentListOf(simOption), expectMostRecentItem().simCardOptions)
            }

            verify { mapper.map(eq(simCard)) }
        }

    private fun createViewModel(
        settingsAvailabilityRepository: SettingsAvailabilityRepository = settingsAvailabilityRepo,
        loadSimCards: LoadSimCards = { emptyFlow() },
        simCardOptionMapper: SimCardOptionMapper = { SimCardOptionFactory.build() },
        loadAccounts: LoadAccounts = { emptyFlow() },
    ) = ImportViewModel(
        settingsAvailabilityRepository = settingsAvailabilityRepository,
        loadSimCards = loadSimCards,
        simCardOptionMapper = simCardOptionMapper,
        loadAccounts = loadAccounts,
    )

    companion object {
        private val SETTINGS_AVAILABILITY = SettingsAvailability(
            areContactsAvailable = true,
            areBlockedNumbersAvailable = true,
            isAboutAvailable = true,
            isImportFromVCardAvailable = true,
        )
    }
}
