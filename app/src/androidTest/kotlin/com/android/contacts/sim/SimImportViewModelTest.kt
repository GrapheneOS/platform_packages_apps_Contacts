package com.android.contacts.sim

import androidx.lifecycle.SavedStateHandle
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.model.SimContactsResult
import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.model.SimCard
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.tests.AccountInfoFactory
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.SimContactFactory
import com.android.contacts.ui.UIIntents
import com.android.contacts.ui.simimport.screen.SimImportViewModel
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun currentAccountIsFirstAccountWhenThereIsNoDefault() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account1 = AccountInfoFactory.build()
            val account2 = AccountInfoFactory.build()
            val viewModel = createViewModel(
                loadAccounts = { flowOf(persistentListOf(account1, account2)) },
            )
            advanceUntilIdle()

            with(viewModel.uiState.value) {
                assertEquals(persistentListOf(account1, account2), accounts)
                assertEquals(account1, currentAccount)
            }
        }

    @Test
    fun defaultsToDefaultAccount() = runTest {
        val account1 = AccountInfoFactory.build()
        val account2 = AccountInfoFactory.build()
        val subject = createViewModel(
            getDefaultAccount = { account2.account },
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
        )
        advanceUntilIdle()

        val state = subject.uiState.value
        assertEquals(persistentListOf(account1, account2), state.accounts)
        assertEquals(account2, state.currentAccount)
    }

    @Test
    fun contactsAreSelectedByDefault() = runTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertEquals(1, contactsToImport.size)
            assertEquals(contact, contactsToImport.first().item)
            assertTrue(contactsToImport.first().isSelected)
        }
    }

    @Test
    fun deselectAndSelectContact() = runTest {
        val account = AccountInfoFactory.build()
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
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.ContactClicked(contact1))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.ContactClicked(contact1))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun deselectAndSelectAllContacts() = runTest {
        val account = AccountInfoFactory.build()
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
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.DeselectAllClicked)
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport.first().isSelected)
            assertFalse(contactsToImport.last().isSelected)
        }

        subject.onAction(Action.SelectAllClicked)
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun keepSelectedContactAcrossAccounts() = runTest {
        val account1 = AccountInfoFactory.build()
        val account2 = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account1, account2)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport.first().isSelected)
        }

        subject.onAction(Action.ContactClicked(contact))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport.first().isSelected)
        }

        subject.onAction(Action.AccountChanged(account2))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertTrue(contactsToImport.first().isSelected)
        }

        subject.onAction(Action.AccountChanged(account1))
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertFalse(contactsToImport.first().isSelected)
        }
    }

    @Test
    fun contactsAlreadyImportedAreSeparate() = runTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = createViewModel(
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = {
                flowOf(
                    SimContactsResult(
                        contacts = persistentListOf(contact),
                        existingContactsInAccounts = persistentMapOf(
                            account.account to setOf(
                                contact,
                            ),
                        ),
                    ),
                )
            },
        )
        advanceUntilIdle()

        with(subject.uiState.value) {
            assertEquals(0, contactsToImport.size)
            assertEquals(1, contactsAlreadyImported.size)
            assertEquals(contact, contactsAlreadyImported.first())
        }
    }

    @Test
    fun importClickStartsImportWithSelectedConfiguration() = runTest {
        val subscriptionId = 2
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        var startSimImportCall: Triple<Int?, List<SimContact>, AccountWithDataSet>? = null
        val subject = createViewModel(
            subscriptionId = subscriptionId,
            loadAccounts = { flowOf(persistentListOf(account)) },
            loadSimContacts = { flowOf(SimContactsResult(contacts = persistentListOf(contact))) },
            startSimImport = { a, b, c -> startSimImportCall = Triple(a, b, c) },
        )

        subject.onAction(Action.ImportClicked)
        advanceUntilIdle()

        startSimImportCall?.let { (callSubscriptionId, callContacts, callAccount) ->
            assertEquals(subscriptionId, callSubscriptionId)
            assertEquals(persistentListOf(contact), callContacts)
            assertEquals(account.account, callAccount)
        }
    }

    private fun createViewModel(
        subscriptionId: Int = SimCard.NO_SUBSCRIPTION_ID,
        getDefaultAccount: GetDefaultAccount = { null },
        loadSimContacts: LoadSimContacts = { emptyFlow() },
        loadAccounts: LoadAccounts = { emptyFlow() },
        startSimImport: StartSimImport = { _, _, _ -> },
    ) = SimImportViewModel(
        SavedStateHandle(persistentMapOf(UIIntents.EXTRA_SUBSCRIPTION_ID to subscriptionId)),
        getDefaultAccount = getDefaultAccount,
        loadSimContacts = loadSimContacts,
        loadAccounts = loadAccounts,
        startSimImport = startSimImport,
    )
}
