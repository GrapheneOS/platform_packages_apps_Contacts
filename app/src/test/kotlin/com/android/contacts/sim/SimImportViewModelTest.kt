package com.android.contacts.sim

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.model.SimContactsResult
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.model.SimContact
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.SimContactFactory
import com.android.contacts.ui.UIIntents
import com.android.contacts.ui.simimport.screen.SimImportViewModel
import com.android.contacts.ui.simimport.screen.mapper.AccountUiModelMapperImpl
import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapperImpl
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import com.android.contacts.ui.simimport.screen.model.SimImportEffect as Effect
import com.android.contacts.ui.simimport.screen.model.SimImportUiState as State
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimImportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Not mocking mappers since they hold no logic
    private val accountUiModelMapper = AccountUiModelMapperImpl()
    private val simContactUiModelMapper = SimContactUiModelMapperImpl()

    @Test
    fun subscriptionId_whenSimCardDoesNotExist_closes() =
        runTest(mainDispatcherRule.testDispatcher) {
            val subscriptionId = 2
            val subject = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(UIIntents.EXTRA_SUBSCRIPTION_ID to subscriptionId),
                ),
                loadSimCards = { flowOf(listOf()) },
            )
            subject.effects.test {
                assertEquals(Effect.Close(isSuccessful = false), awaitItem())
            }
        }

    @Test
    fun isLoading_whenBothLoadAccountsAndContactsFinish_isFalse() =
        runTest(mainDispatcherRule.testDispatcher) {
            val loadAccounts = Channel<List<AccountDisplayModel>>(capacity = Channel.BUFFERED)
            val loadSimContacts = Channel<SimContactsResult>(capacity = Channel.BUFFERED)
            val subject = createViewModel(
                loadAccounts = { loadAccounts.consumeAsFlow() },
                loadSimContacts = { loadSimContacts.consumeAsFlow() },
            )

            subject.uiState.test {
                assertEquals(State.Loading, awaitItem())
                loadAccounts.send(listOf(AccountDisplayModelFactory.build()))
                advanceUntilIdle()
                expectNoEvents()
                loadSimContacts.send(SimContactsResult())
                advanceUntilIdle()
                assertFalse(awaitItem() is State.Loading)
            }
        }

    @Test
    fun state_whenLoadAccountsIsEmpty_isEmptyNoAccounts() =
        runTest(mainDispatcherRule.testDispatcher) {
            val contact = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(emptyList()) },
                loadSimContacts = { flowOf(SimContactsResult(contacts = listOf(contact))) },
            )

            subject.uiState.test {
                advanceUntilIdle()
                assertEquals(State.Empty.NoAccounts, expectMostRecentItem())
            }
        }

    @Test
    fun state_whenLoadSimContactsIsEmpty_isEmptyNoContacts() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = { flowOf(SimContactsResult()) },
            )

            subject.uiState.test {
                advanceUntilIdle()
                assertEquals(State.Empty.NoContacts, expectMostRecentItem())
            }
        }

    @Test
    fun currentAccount_whenThereIsNoDefault_isFirstAccount() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account1 = AccountDisplayModelFactory.build(name = "First")
            val account2 = AccountDisplayModelFactory.build(name = "Second")
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account1, account2)) },
            )

            subject.uiState.test {
                advanceUntilIdle()
                val state = expectMostRecentItem() as State.Ready
                assertEquals(
                    persistentListOf(account1.toUiModel(), account2.toUiModel()),
                    state.accounts,
                )
                assertEquals(account1.toUiModel(), state.currentAccount)
            }
        }

    @Test
    fun currentAccount_onStart_isDefaultAccount() = runTest(mainDispatcherRule.testDispatcher) {
        val account1 = AccountDisplayModelFactory.build(name = "First")
        val account2 = AccountDisplayModelFactory.build(name = "Second")
        val subject = createViewModel(
            getDefaultAccount = { account2.account },
            loadAccounts = { flowOf(listOf(account1, account2)) },
        )

        subject.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as State.Ready
            assertEquals(
                persistentListOf(account1.toUiModel(), account2.toUiModel()),
                state.accounts,
            )
            assertEquals(account2.toUiModel(), state.currentAccount)
        }
    }

    @Test
    fun currentAccount_onAccountsReload_isKept() = runTest(mainDispatcherRule.testDispatcher) {
        val account1 = AccountDisplayModelFactory.build(name = "First")
        val account2 = AccountDisplayModelFactory.build(name = "Second")
        val loadAccountsFlow = MutableStateFlow(listOf(account1, account2))
        val subject = createViewModel(
            getDefaultAccount = { account1.account },
            loadAccounts = { loadAccountsFlow },
        )

        subject.uiState.test {
            advanceUntilIdle()
            assertEquals(
                account1.toUiModel(),
                (expectMostRecentItem() as State.Ready).currentAccount,
            )

            subject.onAction(Action.AccountChanged(loadAccountsFlow.value.last().toUiModel()))
            advanceUntilIdle()
            assertEquals(
                account2.toUiModel(),
                (expectMostRecentItem() as State.Ready).currentAccount,
            )

            val account3 = AccountDisplayModelFactory.build(name = "Third")
            loadAccountsFlow.value = listOf(account1, account2, account3)
            advanceUntilIdle()
            assertEquals(
                account2.toUiModel(),
                (expectMostRecentItem() as State.Ready).currentAccount,
            )
        }
    }

    @Test
    fun selectedContacts_onStart_areAllSelected() = runTest(mainDispatcherRule.testDispatcher) {
        val account = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )

        subject.uiState.test {
            advanceUntilIdle()
            with(expectMostRecentItem() as State.Ready) {
                assertEquals(1, contactsToImport.size)
                assertEquals(contact.toUiModel(), contactsToImport.first().item)
                assertTrue(contactsToImport.first().isSelected)
            }
        }
    }

    @Test
    fun currentAccount_onProcessRestore_isRestored() = runTest(mainDispatcherRule.testDispatcher) {
        val account1 = AccountDisplayModelFactory.build(name = "First")
        val account2 = AccountDisplayModelFactory.build(name = "Second")
        val savedStateHandle = SavedStateHandle()
        val subject1 = createViewModel(
            savedStateHandle = savedStateHandle,
            getDefaultAccount = { account1.account },
            loadAccounts = { flowOf(listOf(account1, account2)) },
        )

        advanceUntilIdle()
        subject1.onAction(Action.AccountChanged(account2.toUiModel()))
        advanceUntilIdle()

        val subject2 = createViewModel(
            savedStateHandle = savedStateHandle,
            getDefaultAccount = { account1.account },
            loadAccounts = { flowOf(listOf(account1, account2)) },
        )
        subject2.uiState.test {
            advanceUntilIdle()
            assertEquals(
                account2.toUiModel(),
                (expectMostRecentItem() as State.Ready).currentAccount,
            )
        }
    }

    @Test
    fun selectedContacts_onAccountsReload_emptyListIsKept() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account1 = AccountDisplayModelFactory.build(name = "First")
            val loadAccountsFlow = MutableStateFlow(listOf(account1))
            val contact = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { loadAccountsFlow },
                loadSimContacts = { flowOf(SimContactsResult(contacts = listOf(contact))) },
            )

            subject.uiState.test {
                advanceUntilIdle()
                assertEquals(1, (expectMostRecentItem() as State.Ready).selectedContactsCount)

                subject.onAction(
                    Action.ContactSelectionChanged(
                        contact = contact.toUiModel(),
                        isSelected = false,
                    ),
                )
                advanceUntilIdle()
                assertEquals(0, (expectMostRecentItem() as State.Ready).selectedContactsCount)

                val account2 = AccountDisplayModelFactory.build(name = "Second")
                loadAccountsFlow.value = listOf(account1, account2)
                advanceUntilIdle()
                assertEquals(0, (expectMostRecentItem() as State.Ready).selectedContactsCount)
            }
        }

    @Test
    fun selectedContacts_onContactSelectionChanges_deselectsAndSelects() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val contact1 = SimContactFactory.build()
            val contact2 = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = {
                    flowOf(SimContactsResult(contacts = listOf(contact1, contact2)))
                },
            )

            subject.uiState.test {
                advanceUntilIdle()
                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                    assertTrue(contactsToImport.last().isSelected)
                }

                subject.onAction(
                    Action.ContactSelectionChanged(
                        contact = contact1.toUiModel(),
                        isSelected = false,
                    ),
                )
                advanceUntilIdle()
                with(expectMostRecentItem() as State.Ready) {
                    assertFalse(contactsToImport.first().isSelected)
                    assertTrue(contactsToImport.last().isSelected)
                }

                subject.onAction(
                    Action.ContactSelectionChanged(
                        contact = contact1.toUiModel(),
                        isSelected = true,
                    ),
                )
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                    assertTrue(contactsToImport.last().isSelected)
                }
            }
        }

    @Test
    fun selectedContacts_onContactSelectionChange_arePersisted() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val contact1 = SimContactFactory.build()
            val contact2 = SimContactFactory.build()
            val savedStateHandle = SavedStateHandle()
            val subject1 = createViewModel(
                savedStateHandle = savedStateHandle,
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = {
                    flowOf(SimContactsResult(contacts = listOf(contact1, contact2)))
                },
            )
            subject1.uiState.test {
                advanceUntilIdle()
                subject1.onAction(
                    Action.ContactSelectionChanged(
                        contact = contact1.toUiModel(),
                        isSelected = false,
                    ),
                )
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            val subject2 = createViewModel(
                savedStateHandle = savedStateHandle,
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = {
                    flowOf(SimContactsResult(contacts = listOf(contact1, contact2)))
                },
            )
            subject2.uiState.test {
                advanceUntilIdle()
                with((expectMostRecentItem() as State.Ready).contactsToImport) {
                    assertFalse(first { it.item == contact1.toUiModel() }.isSelected)
                    assertTrue(first { it.item == contact2.toUiModel() }.isSelected)
                }
            }
        }

    @Test
    fun selectedContacts_onSelectAndDeselectAllClicks_areAllToggled() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val contact1 = SimContactFactory.build()
            val contact2 = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = {
                    flowOf(SimContactsResult(contacts = listOf(contact1, contact2)))
                },
            )

            subject.uiState.test {
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                    assertTrue(contactsToImport.last().isSelected)
                }

                subject.onAction(Action.DeselectAllClicked)
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertFalse(contactsToImport.first().isSelected)
                    assertFalse(contactsToImport.last().isSelected)
                }

                subject.onAction(Action.SelectAllClicked)
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                    assertTrue(contactsToImport.last().isSelected)
                }
            }
        }

    @Test
    fun selectedContacts_onAccountChange_areKeptForEachAccount() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account1 = AccountDisplayModelFactory.build(name = "First")
            val account2 = AccountDisplayModelFactory.build(name = "Second")
            val contact = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account1, account2)) },
                loadSimContacts = { flowOf(SimContactsResult(contacts = listOf(contact))) },
            )

            subject.uiState.test {
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                }

                subject.onAction(
                    Action.ContactSelectionChanged(
                        contact = contact.toUiModel(),
                        isSelected = false,
                    ),
                )
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertFalse(contactsToImport.first().isSelected)
                }

                subject.onAction(Action.AccountChanged(account2.toUiModel()))
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertTrue(contactsToImport.first().isSelected)
                }

                subject.onAction(Action.AccountChanged(account1.toUiModel()))
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertFalse(contactsToImport.first().isSelected)
                }
            }
        }

    @Test
    fun contactsAlreadyImportant_onStart_areFilteredFromContactsToImport() =
        runTest(mainDispatcherRule.testDispatcher) {
            val account = AccountDisplayModelFactory.build()
            val contact = SimContactFactory.build()
            val subject = createViewModel(
                loadAccounts = { flowOf(listOf(account)) },
                loadSimContacts = {
                    flowOf(
                        SimContactsResult(
                            contacts = listOf(contact),
                            existingContactsInAccounts = mapOf(account.account to setOf(contact)),
                        ),
                    )
                },
            )

            subject.uiState.test {
                advanceUntilIdle()

                with(expectMostRecentItem() as State.Ready) {
                    assertEquals(0, contactsToImport.size)
                    assertEquals(1, contactsAlreadyImported.size)
                    assertEquals(contact.toUiModel(), contactsAlreadyImported.first())
                }
            }
        }

    @Test
    fun startSimImport_onImportClick_isCalled() = runTest(mainDispatcherRule.testDispatcher) {
        val subscriptionId = 2
        val account = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        val startSimImport = mockk<StartSimImport>(relaxed = true)
        val subject = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(UIIntents.EXTRA_SUBSCRIPTION_ID to subscriptionId),
            ),
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = listOf(contact))) },
            startSimImport = startSimImport,
        )
        subject.uiState.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        subject.effects.test {
            subject.onAction(Action.ImportClicked)
            advanceUntilIdle()
            assertEquals(Effect.Close(isSuccessful = true), awaitItem())
        }
        verify {
            startSimImport(subscriptionId, listOf(contact), account.account)
        }
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        loadSimCards: LoadSimCards = { emptyFlow() },
        getDefaultAccount: GetDefaultAccount = { null },
        loadSimContacts: LoadSimContacts = {
            flowOf(SimContactsResult(contacts = listOf(SimContactFactory.build())))
        },
        loadAccounts: LoadAccounts = {
            flowOf(listOf(AccountDisplayModelFactory.build()))
        },
        startSimImport: StartSimImport = { _, _, _ -> },
    ) = SimImportViewModel(
        savedStateHandle,
        getDefaultAccount = getDefaultAccount,
        loadSimCards = loadSimCards,
        loadSimContacts = loadSimContacts,
        loadAccounts = loadAccounts,
        startSimImport = startSimImport,
        accountUiModelMapper = accountUiModelMapper,
        simContactUiModelMapper = simContactUiModelMapper,
    )

    private fun AccountDisplayModel.toUiModel() = accountUiModelMapper.map(this)
    private fun SimContact.toUiModel() = simContactUiModelMapper.map(this)
}
