package com.android.contacts.domain.accounts.usecase

import app.cash.turbine.test
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.tests.AccountDisplayModelFactory
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoadWritableAccountsImplTest {

    private val accountsChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val accountTypeManager = mockk<AccountTypeManager>()
    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>()

    @Test
    fun invoke_whenAccountsAreLoaded_emitsMappedAccounts() = runTest {
        val firstAccount = mockk<AccountInfo>()
        val secondAccount = mockk<AccountInfo>()
        val firstModel = AccountDisplayModelFactory.build(name = "first@example.org")
        val secondModel = AccountDisplayModelFactory.build(name = "second@example.org")
        givenAccounts(firstAccount, secondAccount)
        givenMapping(firstAccount to firstModel, secondAccount to secondModel)

        createUseCase().invoke().test {
            assertEquals(persistentListOf(firstModel, secondModel), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenThereAreNoAccounts_emitsEmptyList() = runTest {
        givenAccounts()

        createUseCase().invoke().test {
            assertEquals(persistentListOf<AccountDisplayModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenAccountsChangeBroadcastIsReceived_reloadsAccounts() = runTest {
        val account = mockk<AccountInfo>()
        val model = AccountDisplayModelFactory.build()
        givenAccounts(account)
        givenMapping(account to model)

        createUseCase().invoke().test {
            assertEquals(persistentListOf(model), awaitItem())

            val reloadedModel = AccountDisplayModelFactory.build(name = "reloaded@example.org")
            givenMapping(account to reloadedModel)
            accountsChanged.emit(Unit)

            assertEquals(persistentListOf(reloadedModel), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenLoadingFails_emitsEmptyList() = runTest {
        every { accountTypeManager.filterAccountsAsync(any()) } returns
            Futures.immediateFailedFuture(IllegalStateException("accounts unavailable"))

        createUseCase().invoke().test {
            assertEquals(persistentListOf<AccountDisplayModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenLoadingIsInterrupted_emitsEmptyList() = runTest {
        val future = mockk<ListenableFuture<List<AccountInfo>>>()
        every { future.get() } throws InterruptedException()
        every { accountTypeManager.filterAccountsAsync(any()) } returns future

        createUseCase().invoke().test {
            assertEquals(persistentListOf<AccountDisplayModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenAccountsAreNull_emitsEmptyList() = runTest {
        every { accountTypeManager.filterAccountsAsync(any()) } returns
            Futures.immediateFuture(null)

        createUseCase().invoke().test {
            assertEquals(persistentListOf<AccountDisplayModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun givenAccounts(vararg accounts: AccountInfo) {
        every { accountTypeManager.filterAccountsAsync(any()) } returns
            Futures.immediateFuture(accounts.toList())
    }

    private fun givenMapping(vararg mappings: Pair<AccountInfo, AccountDisplayModel>) {
        mappings.forEach { (accountInfo, model) ->
            every { accountDisplayModelMapper.map(accountInfo) } returns model
        }
    }

    private fun createUseCase(): LoadWritableAccountsImpl {
        return LoadWritableAccountsImpl(
            buildBroadcastReceiverFlow = BuildBroadcastReceiverFlow { accountsChanged },
            accountTypeManager = accountTypeManager,
            accountDisplayModelMapper = accountDisplayModelMapper,
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )
    }
}
