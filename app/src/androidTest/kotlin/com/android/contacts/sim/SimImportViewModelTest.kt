package com.android.contacts.sim

import com.android.contacts.model.SimCard
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.sim.ui.SimImportViewModel
import com.android.contacts.tests.AccountInfoFactory
import com.android.contacts.tests.SimContactFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimImportViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun currentAccountIsFirstAccountWhenThereIsNoDefault() = runTest {
        val account1 = AccountInfoFactory.build()
        val account2 = AccountInfoFactory.build()
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account1, account2)) },
        )

        val state = subject.state.value
        assertEquals(listOf(account1, account2), state.accounts)
        assertEquals(account1, state.currentAccount)
    }

    @Test
    fun defaultsToDefaultAccount() = runTest {
        val account1 = AccountInfoFactory.build()
        val account2 = AccountInfoFactory.build()
        val subject = buildViewModel(
            getDefaultAccount = { account2.account },
            loadAccounts = { flowOf(listOf(account1, account2)) },
        )

        val state = subject.state.value
        assertEquals(listOf(account1, account2), state.accounts)
        assertEquals(account2, state.currentAccount)
    }

    @Test
    fun contactsAreSelectedByDefault() = runTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = { flowOf(LoadSimContacts.Result(contacts = listOf(contact))) },
        )

        with(subject.state.value) {
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
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = {
                flowOf(LoadSimContacts.Result(contacts = listOf(contact1, contact2)))
            },
        )

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.ContactClicked(contact1))

        with(subject.state.value) {
            assertFalse(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.ContactClicked(contact1))

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun deselectAndSelectAllContacts() = runTest {
        val account = AccountInfoFactory.build()
        val contact1 = SimContactFactory.build()
        val contact2 = SimContactFactory.build()
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = {
                flowOf(LoadSimContacts.Result(contacts = listOf(contact1, contact2)))
            },
        )

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.DeselectAllClicked)

        with(subject.state.value) {
            assertFalse(contactsToImport.first().isSelected)
            assertFalse(contactsToImport.last().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.SelectAllClicked)

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
            assertTrue(contactsToImport.last().isSelected)
        }
    }

    @Test
    fun keepSelectedContactAcrossAccounts() = runTest {
        val account1 = AccountInfoFactory.build()
        val account2 = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account1, account2)) },
            loadSimContacts = { flowOf(LoadSimContacts.Result(contacts = listOf(contact))) },
        )

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.ContactClicked(contact))

        with(subject.state.value) {
            assertFalse(contactsToImport.first().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.AccountChanged(account2))

        with(subject.state.value) {
            assertTrue(contactsToImport.first().isSelected)
        }

        subject.onEvent(SimImportViewModel.Event.AccountChanged(account1))

        with(subject.state.value) {
            assertFalse(contactsToImport.first().isSelected)
        }
    }

    @Test
    fun contactsAlreadyImportedAreSeparate() = runTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        val subject = buildViewModel(
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = {
                flowOf(
                    LoadSimContacts.Result(
                        contacts = listOf(contact),
                        existingContactsInAccounts = mapOf(account.account to setOf(contact)),
                    ),
                )
            },
        )

        with(subject.state.value) {
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
        val subject = buildViewModel(
            subscriptionId = subscriptionId,
            loadAccounts = { flowOf(listOf(account)) },
            loadSimContacts = { flowOf(LoadSimContacts.Result(contacts = listOf(contact))) },
            startSimImport = { a, b, c -> startSimImportCall = Triple(a, b, c) },
        )

        subject.onEvent(SimImportViewModel.Event.ImportClicked)

        startSimImportCall?.let { (callSubscriptionId, callContacts, callAccount) ->
            assertEquals(subscriptionId, callSubscriptionId)
            assertEquals(listOf(contact), callContacts)
            assertEquals(account.account, callAccount)
        }
    }

    private fun buildViewModel(
        subscriptionId: Int = SimCard.NO_SUBSCRIPTION_ID,
        getDefaultAccount: () -> AccountWithDataSet? = { null },
        loadSimContacts: (Int) -> Flow<LoadSimContacts.Result> = { emptyFlow() },
        loadAccounts: () -> Flow<List<AccountInfo>> = { emptyFlow() },
        startSimImport: (Int, List<SimContact>, AccountWithDataSet) -> Unit = { _, _, _ -> },
    ) = SimImportViewModel(
        subscriptionId = subscriptionId,
        getDefaultAccount = getDefaultAccount,
        loadSimContacts = loadSimContacts,
        loadAccounts = loadAccounts,
        startSimImport = startSimImport,
    )
}
