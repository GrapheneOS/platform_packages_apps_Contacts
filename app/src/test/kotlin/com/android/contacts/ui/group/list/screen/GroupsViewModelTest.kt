package com.android.contacts.ui.group.list.screen

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupFactory
import com.android.contacts.ui.group.list.GroupsActivity
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapper
import com.android.contacts.ui.group.list.screen.model.AccountGroupsItem
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import com.android.contacts.ui.group.list.screen.model.GroupsAction as Action
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect
import com.android.contacts.ui.group.list.screen.model.GroupsUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getGroups = mockk<GetGroups>(relaxed = true)
    private val loadAccounts = mockk<LoadAccounts>(relaxed = true)
    private val groupsUiMapper = mockk<GroupsUiMapper>(relaxed = true)
    private val getDefaultAccount = mockk<GetDefaultAccount>(relaxed = true)
    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(ContentUris::class)
        every { getGroups(any(), any()) } returns emptyFlow()
        every { loadAccounts(any()) } returns emptyFlow()
        every { getDefaultAccount() } returns DEFAULT_ACCOUNT
        every {
            groupsUiMapper.map(any(), any(), any())
        } returns persistentListOf()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenGroupsFailToLoad_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            every { getGroups(any(), any()) } returns flowOf(null)
            every { loadAccounts(any()) } returns flowOf(emptyList())
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.effects.test {
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun whenGroupsAndAccountsAreLoading_stateIsLoading() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(State.Loading, awaitItem())
            }
        }

    @Test
    fun whenThereAreNoGroupItems_stateIsWithoutAccounts() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenAccountGroups(emptyList())
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(State.WithoutAccounts, expectMostRecentItem())
            }
        }

    @Test
    fun whenThereAreGroupItems_stateIsWithAccounts() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val accountGroups = givenAccountGroups(listOf(ACCOUNT_GROUPS))
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    State.WithAccounts(
                        groups = accountGroups,
                        showAccountHeaders = true,
                    ),
                    expectMostRecentItem(),
                )
            }
        }

    @Test
    fun whenAccountIsProvided_getGroupsByAccountAndShowAccountHeadersIsFalse() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val accountGroups = givenAccountGroups(listOf(ACCOUNT_GROUPS))
            val viewModel = createViewModel(
                initialState = mapOf(
                    GroupsActivity.EXTRA_ACCOUNT to ACCOUNT_GROUPS.account,
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    State.WithAccounts(
                        groups = accountGroups,
                        showAccountHeaders = false,
                    ),
                    expectMostRecentItem(),
                )
            }

            val filters = slot<List<GroupFilter>>()
            verify { getGroups(capture(filters), GroupSort.BY_TITLE) }
            assertTrue(filters.captured.contains(GroupFilter.ByAccount(ACCOUNT_GROUPS.account)))
            assertTrue(filters.captured.contains(GroupFilter.Favorites(false)))
            assertTrue(filters.captured.contains(GroupFilter.AutoAdd(false)))
        }

    @Test
    fun whenAccountIsProvided_useItAsTheMapperDefaultAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenAccountGroups(listOf(ACCOUNT_GROUPS))
            val viewModel = createViewModel(
                initialState = mapOf(
                    GroupsActivity.EXTRA_ACCOUNT to ACCOUNT_GROUPS.account,
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            verify { groupsUiMapper.map(any(), any(), ACCOUNT_GROUPS.account) }
        }

    @Test
    fun whenNoAccountIsProvided_mapWithTheDefaultAccount() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenAccountGroups(listOf(ACCOUNT_GROUPS))
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            verify { groupsUiMapper.map(any(), any(), DEFAULT_ACCOUNT) }
        }

    @Test
    fun whenThereIsOnlyOneNullAccount_showAccountHeadersIsFalse() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val accountGroups = givenAccountGroups(
                listOf(
                    ACCOUNT_GROUPS.copy(account = AccountModel(null, null, null)),
                ),
            )
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    State.WithAccounts(
                        groups = accountGroups,
                        showAccountHeaders = false,
                    ),
                    expectMostRecentItem(),
                )
            }
        }

    @Test
    fun whenNewClicked_createNewGroup() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onAction(Action.NewClicked(null))
            viewModel.effects.test {
                assertEquals(Effect.CreateNewGroup(null), awaitItem())
            }
        }

    @Test
    fun whenGroupClicked_openGroup() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val groupUiItem = GroupUiItem(1L, "", 0)
            val groupUri = mockk<Uri>()
            every { ContentUris.withAppendedId(any(), any()) } returns groupUri

            val viewModel = createViewModel()

            viewModel.onAction(Action.GroupClicked(groupUiItem))
            viewModel.effects.test {
                assertEquals(Effect.OpenGroup(groupUri), awaitItem())
            }
            verify {
                ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, groupUiItem.id)
            }
        }

    private fun createViewModel(
        initialState: Map<String, Any?> = emptyMap(),
    ): GroupsScreenModel {
        return GroupsViewModel(
            savedStateHandle = SavedStateHandle(initialState),
            getGroups = getGroups,
            loadAccounts = loadAccounts,
            groupsUiMapper = groupsUiMapper,
            getDefaultAccount = getDefaultAccount,
            accountDisplayModelMapper = accountDisplayModelMapper,
        )
    }

    private fun givenGroups(groups: List<Group>): List<Group> {
        every { getGroups(any(), any()) } returns flowOf(groups)
        return groups
    }

    private fun givenInsertableAccounts(
        accounts: List<AccountDisplayModel>,
    ): List<AccountDisplayModel> {
        every { loadAccounts(any()) } returns flowOf(accounts)
        return accounts
    }

    private fun givenAccountGroups(
        accountGroups: List<AccountGroupsItem>,
    ): ImmutableList<AccountGroupsItem> {
        val groups = givenGroups(listOf(GroupFactory.build()))
        val accounts = givenInsertableAccounts(listOf(AccountDisplayModelFactory.build()))
        every { groupsUiMapper.map(groups, accounts, any()) } returns
            accountGroups.toImmutableList()
        return accountGroups.toImmutableList()
    }

    companion object {
        private val ACCOUNT = AccountModelFactory.build(name = "Account")
        private val GROUP = GroupUiItem(
            id = 123L,
            name = "Group",
            summaryCount = 12,
        )
        private val ACCOUNT_GROUPS = AccountGroupsItem(
            account = ACCOUNT,
            accountName = ACCOUNT.name!!,
            groups = persistentListOf(GROUP),
            canCreateGroup = true,
        )
        private val DEFAULT_ACCOUNT = AccountModelFactory.build(name = "Default")
    }
}
