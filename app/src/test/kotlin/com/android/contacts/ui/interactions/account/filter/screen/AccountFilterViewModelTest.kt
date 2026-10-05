package com.android.contacts.ui.interactions.account.filter.screen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.data.contacts.model.ContactsCount
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.domain.accounts.usecase.LoadAccountsWithContactsCount
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.interactions.account.filter.AccountFilterActivity
import com.android.contacts.ui.interactions.account.filter.screen.mapper.AccountFilterItemMapper
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterAction as Action
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterEffect as Effect
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountFilterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loadAccountsWithContactsCount = mockk<LoadAccountsWithContactsCount>(relaxed = true)
    private val accountFilterItemMapper = mockk<AccountFilterItemMapper>(relaxed = true)

    @Test
    fun mapsItemsToState() = runTest {
        val accountsMap = onAccountsMap()
        val item = AccountFilterItem.All(0)
        onItems(listOf(item))

        createViewModel().uiState.test {
            assertEquals(State.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(
                State.Ready(
                    persistentListOf(
                        SelectableItem(
                            item = item,
                            isSelected = true,
                        ),
                    ),
                ),
                awaitItem(),
            )
        }

        verify { loadAccountsWithContactsCount(AccountFilter.CONTACTS_INSERTABLE) }
        verify { accountFilterItemMapper.map(accountsMap) }
    }

    @Test
    fun selectedSelectedAccount() = runTest {
        val account = AccountDisplayModelFactory.build()
        val accountsMap = onAccountsMap(all = 1, mapOf(account to 1))
        val allItem = AccountFilterItem.All(1)
        val accountFilter = ContactsAccountFilter.One(account.account)
        val accountItem = AccountFilterItem.One(
            name = account.name!!,
            filter = accountFilter,
        )
        onItems(listOf(allItem, accountItem))

        val viewModel = createViewModel(selectedFilter = accountFilter)
        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(
                State.Ready(
                    persistentListOf(
                        SelectableItem(
                            item = allItem,
                            isSelected = false,
                        ),
                        SelectableItem(
                            item = accountItem,
                            isSelected = true,
                        ),
                    ),
                ),
                expectMostRecentItem(),
            )
        }

        verify { loadAccountsWithContactsCount(AccountFilter.CONTACTS_INSERTABLE) }
        verify { accountFilterItemMapper.map(accountsMap) }
    }

    @Test
    fun onDismiss_close() = runTest {
        onAccountsMap()
        val allItem = AccountFilterItem.All(1)
        onItems(listOf(allItem))

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.Dismiss)
            advanceUntilIdle()
            assertEquals(Effect.Close(filterPicked = null), awaitItem())
        }
    }

    @Test
    fun onItemClick_closeWithSelection() = runTest {
        onAccountsMap()
        val allItem = AccountFilterItem.All(1)
        onItems(listOf(allItem))

        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.ItemClicked(allItem))
            advanceUntilIdle()
            assertEquals(Effect.Close(filterPicked = allItem.filter), awaitItem())
        }
    }

    private fun createViewModel(
        selectedFilter: ContactsAccountFilter? = null,
    ): AccountFilterScreenModel {
        return AccountFilterViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(AccountFilterActivity.EXTRA_FILTER to selectedFilter),
            ),
            loadAccountsWithContactsCount = loadAccountsWithContactsCount,
            accountFilterItemMapper = accountFilterItemMapper,
        )
    }

    private fun onAccountsMap(
        all: Int = 0,
        accountsMap: Map<AccountDisplayModel, Int> = emptyMap(),
    ): ContactsCount<AccountDisplayModel> {
        val counts = ContactsCount(
            all = all,
            byAccount = accountsMap,
        )
        every { loadAccountsWithContactsCount(any()) } returns flowOf(counts)
        return counts
    }

    private fun onItems(
        items: List<AccountFilterItem> = emptyList(),
    ): List<AccountFilterItem> {
        every { accountFilterItemMapper.map(any()) } returns items
        return items
    }
}
