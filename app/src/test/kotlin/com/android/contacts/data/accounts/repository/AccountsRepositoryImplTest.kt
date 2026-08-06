package com.android.contacts.data.accounts.repository

import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.preference.ContactsPreferences
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AccountsRepositoryImplTest {

    private val contactsPreferences = mockk<ContactsPreferences>()
    private val accountTypeManager = mockk<AccountTypeManager>()

    private val repository = AccountsRepositoryImpl(
        contactsPreferences = contactsPreferences,
        accountTypeManager = accountTypeManager,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun getDefaultAccountLabel_whenDefaultAccountIsWritable_returnsItsNameLabel() = runTest {
        givenDefaultAccount(DEFAULT_ACCOUNT)
        givenWritableAccounts(
            accountInfo(OTHER_ACCOUNT, label = "other@example.org"),
            accountInfo(DEFAULT_ACCOUNT, label = "Device"),
        )

        assertEquals("Device", repository.getDefaultAccountLabel())
    }

    @Test
    fun getDefaultAccountLabel_whenThereIsNoDefaultAccount_returnsNull() = runTest {
        givenDefaultAccount(null)

        assertNull(repository.getDefaultAccountLabel())
    }

    @Test
    fun getDefaultAccountLabel_whenDefaultAccountIsNotWritable_returnsNull() = runTest {
        givenDefaultAccount(DEFAULT_ACCOUNT)
        givenWritableAccounts(accountInfo(OTHER_ACCOUNT, label = "other@example.org"))

        assertNull(repository.getDefaultAccountLabel())
    }

    @Test
    fun getDefaultAccountLabel_whenThereAreNoWritableAccounts_returnsNull() = runTest {
        givenDefaultAccount(DEFAULT_ACCOUNT)
        givenWritableAccounts()

        assertNull(repository.getDefaultAccountLabel())
    }

    @Test
    fun getDefaultAccountLabel_whenLoadingAccountsFails_returnsNull() = runTest {
        givenDefaultAccount(DEFAULT_ACCOUNT)
        every { accountTypeManager.filterAccountsAsync(any()) } returns
            Futures.immediateFailedFuture(IllegalStateException("accounts unavailable"))

        assertNull(repository.getDefaultAccountLabel())
    }

    @Test
    fun getDefaultAccountLabel_whenLoadingAccountsIsInterrupted_returnsNull() = runTest {
        val future = mockk<ListenableFuture<List<AccountInfo>>>()
        every { future.get() } throws InterruptedException()
        givenDefaultAccount(DEFAULT_ACCOUNT)
        every { accountTypeManager.filterAccountsAsync(any()) } returns future

        assertNull(repository.getDefaultAccountLabel())
    }

    private fun givenDefaultAccount(account: AccountWithDataSet?) {
        every { contactsPreferences.defaultAccount } returns account
    }

    private fun givenWritableAccounts(vararg accounts: AccountInfo) {
        every { accountTypeManager.filterAccountsAsync(any()) } returns
            Futures.immediateFuture(accounts.toList())
    }

    private fun accountInfo(
        account: AccountWithDataSet,
        label: String,
    ): AccountInfo {
        val accountInfo = mockk<AccountInfo>()
        every { accountInfo.account } returns account
        every { accountInfo.nameLabel } returns label
        return accountInfo
    }

    private companion object {
        val DEFAULT_ACCOUNT = AccountWithDataSet("default@example.org", "com.example", null)
        val OTHER_ACCOUNT = AccountWithDataSet("other@example.org", "com.example", null)
    }
}
