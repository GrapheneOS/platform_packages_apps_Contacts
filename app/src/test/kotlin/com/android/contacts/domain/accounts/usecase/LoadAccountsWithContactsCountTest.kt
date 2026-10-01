package com.android.contacts.domain.accounts.usecase

import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadAccountsWithContactsCountTest {

    private val loadAccounts = mockk<LoadAccounts>(relaxed = true)
    private val contactsRepository = mockk<ContactsRepository>(relaxed = true)

    private val subject: LoadAccountsWithContactsCount = LoadAccountsWithContactsCountImpl(
        loadAccounts = loadAccounts,
        contactsRepository = contactsRepository,
    )

    @Test
    fun combinesAccountsWithCounts() = runTest {
        val account1 = AccountDisplayModelFactory.build(name = "Account 1")
        val account2 = AccountDisplayModelFactory.build(name = "Account 2")
        onAccounts(account1, account2)
        onContactsCount(
            account1.account to 1,
            account2.account to 10,
        )

        val result = subject(AccountFilter.CONTACTS_INSERTABLE).first()

        assertEquals(2, result.size)
        assertEquals(1, result[account1])
        assertEquals(10, result[account2])
    }

    @Test
    fun withCountReturnsZero() = runTest {
        val account = AccountDisplayModelFactory.build()
        onAccounts(account)
        onContactsCount()

        val result = subject(AccountFilter.CONTACTS_INSERTABLE).first()

        assertEquals(1, result.size)
        assertEquals(0, result[account])
    }

    private fun onAccounts(vararg accounts: AccountDisplayModel) {
        every { loadAccounts(any()) } returns flowOf(accounts.toList())
    }

    private fun onContactsCount(vararg entries: Pair<AccountModel, Int>) {
        every { contactsRepository.getContactsCount() } returns flowOf(entries.toMap())
    }
}
