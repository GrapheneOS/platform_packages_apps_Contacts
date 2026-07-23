package com.android.contacts.ui.simimport

import androidx.lifecycle.SavedStateHandle
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.model.SimContactsResult
import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.model.SimContact
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.SimContactFactory
import com.android.contacts.ui.UIIntents
import com.android.contacts.ui.simimport.screen.SimImportViewModel
import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapperImpl
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimImportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Not mocking this mapper since it holds no logic
    private val simContactUiModelMapper = SimContactUiModelMapperImpl()

    @Test
    fun isLoading_whenBothLoadAccountsAndContactsFinish_isFalse() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val loadAccountsFlow = MutableSharedFlow<ImmutableList<AccountDisplayModel>>()
            val loadSimContactsFlow = MutableSharedFlow<SimContactsResult>()
            val viewModel = createViewModel(
                loadAccounts = { loadAccountsFlow },
                loadSimContacts = { loadSimContactsFlow },
            )

            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isLoading)

            loadAccountsFlow.emit(persistentListOf(AccountDisplayModelFactory.build()))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isLoading)

            loadSimContactsFlow.emit(SimContactsResult())
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun currentAccount_whenThereIsNoDefault_isFirstAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account1 = AccountDisplayModelFactory.build()
            val account2 = AccountDisplayModelFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account1, account2)) },
            )
            advanceUntilIdle()

            with(viewModel.uiState.value) {
                assertEquals(
                    persistentListOf(AccountUiModel(account1), AccountUiModel(account2)),
                    accounts,
                )
                assertEquals(AccountUiModel(account1), currentAccount)
            }
        }

    @Test
    fun currentAccount_onStart_isDefaultAccount() = runTest {
        val account1 = AccountDisplayModelFactory.build()
        val account2 = AccountDisplayModelFactory.build()
        val subject = createViewModel(
            getDefaultAccount = { account2.toUiModel() },
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
        )
        advanceUntilIdle()

        val state = subject.uiState.value
        assertEquals(
            persistentListOf(AccountUiModel(account1), AccountUiModel(account2)),
            state.accounts,
        )
        assertEquals(AccountUiModel(account2), state.currentAccount)
    }

    @Test
    fun currentAccount_onAccountsReload_isKept() = runTest {
        val account1 = AccountDisplayModelFactory.build()
        val account2 = AccountDisplayModelFactory.build()
        val loadAccountsFlow = MutableStateFlow(persistentListOf(account1, account2))
        val subject = createViewModel(
            getDefaultAccount = { account1.toUiModel() },
            loadAccounts = { loadAccountsFlow },
        )

        advanceUntilIdle()
        assertEquals(AccountUiModel(account1), subject.uiState.value.currentAccount)

        subject.onAction(Action.AccountChanged(AccountUiModel(loadAccountsFlow.value.last())))
        advanceUntilIdle()
        assertEquals(AccountUiModel(account2), subject.uiState.value.currentAccount)

        val account3 = AccountDisplayModelFactory.build()
        loadAccountsFlow.value = persistentListOf(account1, account2, account3)
        advanceUntilIdle()
        assertEquals(AccountUiModel(account2), subject.uiState.value.currentAccount)
    }

    @Test
    fun selectedContacts_onStart_areAllSelected() = runTest {
        val account = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertEquals(1, contactsToImport!!.size)
            assertEquals(contact.toUiModel(), contactsToImport.first().item)
            assertTrue(contactsToImport.first().isSelected)
        }
    }

    @Test
    fun currentAccount_onProcessRestore_isRestored() = runTest {
        val account1 = AccountDisplayModelFactory.build()
        val account2 = AccountDisplayModelFactory.build()
        val savedStateHandle = SavedStateHandle()
        val subject1 = createViewModel(
            savedStateHandle = savedStateHandle,
            getDefaultAccount = { account1.toUiModel() },
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
        )
        advanceUntilIdle()

        subject1.onAction(Action.AccountChanged(AccountUiModel(account2)))
        advanceUntilIdle()

        val subject2 = createViewModel(
            savedStateHandle = savedStateHandle,
            getDefaultAccount = { account1.toUiModel() },
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
        )
        advanceUntilIdle()
        assertEquals(AccountUiModel(account2), subject2.uiState.value.currentAccount)
    }

    @Test
    fun selectedContacts_onAccountsReload_emptyListIsKept() = runTest {
        val account1 = AccountDisplayModelFactory.build()
        val loadAccountsFlow = MutableStateFlow(persistentListOf(account1))
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { loadAccountsFlow },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )
        advanceUntilIdle()
        assertEquals(1, subject.uiState.value.selectedContactsCount)

        subject.onAction(
            Action.ContactSelectionChanged(contact = contact.toUiModel(), isSelected = false),
        )
        advanceUntilIdle()
        assertEquals(0, subject.uiState.value.selectedContactsCount)

        val account2 = AccountDisplayModelFactory.build()
        loadAccountsFlow.value = persistentListOf(account1, account2)
        advanceUntilIdle()
        assertEquals(0, subject.uiState.value.selectedContactsCount)
    }

    @Test
    fun selectedContacts_onContactSelectionChanges_deselectsAndSelects() = runTest {
        val account = AccountDisplayModelFactory.build()
        val contact1 = SimContactFactory.build()
        val contact2 = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(SimContactsResult(contacts = persistentListOf(contact1, contact2)))
            },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(
            Action.ContactSelectionChanged(contact = contact1.toUiModel(), isSelected = false),
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport!!.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(
            Action.ContactSelectionChanged(contact = contact1.toUiModel(), isSelected = true),
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun selectedContacts_onContactSelectionChange_arePersisted() = runTest {
        val account = AccountDisplayModelFactory.build()
        val contact1 = SimContactFactory.build()
        val contact2 = SimContactFactory.build()
        val savedStateHandle = SavedStateHandle()
        val subject1 = createViewModel(
            savedStateHandle = savedStateHandle,
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(SimContactsResult(contacts = persistentListOf(contact1, contact2)))
            },
        )
        advanceUntilIdle()

        subject1.onAction(
            Action.ContactSelectionChanged(contact = contact1.toUiModel(), isSelected = false),
        )
        advanceUntilIdle()

        val subject2 = createViewModel(
            savedStateHandle = savedStateHandle,
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(SimContactsResult(contacts = persistentListOf(contact1, contact2)))
            },
        )
        advanceUntilIdle()

        with(subject2.uiState.value.contactsToImport!!) {
            assertFalse(first { it.item == contact1.toUiModel() }.isSelected)
            assertTrue(first { it.item == contact2.toUiModel() }.isSelected)
        }
    }

    @Test
    fun selectedContacts_onSelectAndDeselectAllClicks_areAllToggled() = runTest {
        val account = AccountDisplayModelFactory.build()
        val contact1 = SimContactFactory.build()
        val contact2 = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(SimContactsResult(contacts = persistentListOf(contact1, contact2)))
            },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.DeselectAllClicked)
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport!!.first().isSelected)
            assertFalse(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.SelectAllClicked)
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun selectedContacts_onAccountChange_areKeptForEachAccount() = runTest {
        val account1 = AccountDisplayModelFactory.build()
        val account2 = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
        }

        subject.onAction(
            Action.ContactSelectionChanged(contact = contact.toUiModel(), isSelected = false),
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport!!.first().isSelected)
        }

        subject.onAction(Action.AccountChanged(AccountUiModel(account2)))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport!!.first().isSelected)
        }

        subject.onAction(Action.AccountChanged(AccountUiModel(account1)))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport!!.first().isSelected)
        }
    }

    @Test
    fun contactsAlreadyImportant_onStart_areFilteredFromContactsToImport() = runTest {
        val account = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(
                    SimContactsResult(
                        contacts = persistentListOf(contact),
                        existingContactsInAccounts = persistentMapOf(
                            account.toUiModel() to setOf(contact),
                        ),
                    ),
                )
            },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertEquals(0, contactsToImport!!.size)
            assertEquals(1, contactsAlreadyImported!!.size)
            assertEquals(contact.toUiModel(), contactsAlreadyImported.first())
        }
    }

    @Test
    fun startSimImport_onImportClick_isCalled() = runTest {
        val subscriptionId = 2
        val account = AccountDisplayModelFactory.build()
        val contact = SimContactFactory.build()
        var startSimImportCall: Triple<Int?, List<SimContact>, AccountModel>? = null
        val subject = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(UIIntents.EXTRA_SUBSCRIPTION_ID to subscriptionId),
            ),
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
            startSimImport = { a, b, c -> startSimImportCall = Triple(a, b, c) },
        )
        advanceUntilIdle()

        subject.onAction(Action.ImportClicked)
        advanceUntilIdle()

        assertNotNull(startSimImportCall)
        startSimImportCall!!.let { (callSubscriptionId, callContacts, callAccount) ->
            assertEquals(subscriptionId, callSubscriptionId)
            assertEquals(persistentListOf(contact), callContacts)
            assertEquals(account.toUiModel(), callAccount)
        }
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        getDefaultAccount: GetDefaultAccount = { null },
        loadSimContacts: LoadSimContacts = { emptyFlow() },
        loadAccounts: LoadAccounts = { emptyFlow() },
        startSimImport: StartSimImport = { _, _, _ -> },
    ) = SimImportViewModel(
        savedStateHandle,
        getDefaultAccount = getDefaultAccount,
        loadSimContacts = loadSimContacts,
        loadAccounts = loadAccounts,
        startSimImport = startSimImport,
        simContactUiModelMapper = simContactUiModelMapper,
    )

    private fun AccountDisplayModel.toUiModel() = AccountModel(
        name = name,
        type = type,
        dataSet = dataSet,
    )

    private fun SimContact.toUiModel() = simContactUiModelMapper.map(this)
}
