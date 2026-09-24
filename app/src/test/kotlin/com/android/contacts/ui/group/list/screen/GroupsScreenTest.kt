package com.android.contacts.ui.group.list.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.tests.AccountModelFactory
import com.android.contacts.ui.group.list.screen.model.AccountGroupsItem
import com.android.contacts.ui.group.list.screen.model.GROUPS_ACCOUNT_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_LOADING_TEST_TAG
import com.android.contacts.ui.group.list.screen.model.GROUPS_NEW_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_NO_ACCOUNTS_TEST_TAG
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import com.android.contacts.ui.group.list.screen.model.GroupsAction as Action
import com.android.contacts.ui.group.list.screen.model.GroupsUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupsScreenTest {

    private val screenModel = mockk<GroupsScreenModel>(relaxed = true)

    @Test
    fun whenLoading_showLoading() = runComposeUiTest {
        withState(State.Loading)
        setScreenContent()

        onNodeWithTag(GROUPS_LOADING_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun whenWithoutAccounts_showNoAccounts() = runComposeUiTest {
        withState(State.WithoutAccounts)
        setScreenContent()

        onNodeWithTag(GROUPS_NO_ACCOUNTS_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun whenShowAccountHeadersIsTrue_showAccountHeaders() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_ACCOUNT_TEST_TAG_PREFIX + ACCOUNT.name).assertIsDisplayed()
    }

    @Test
    fun whenShowAccountHeadersIsFalse_doNotShowAccountHeaders() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS),
                showAccountHeaders = false,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_ACCOUNT_TEST_TAG_PREFIX + ACCOUNT.name).assertIsNotDisplayed()
    }

    @Test
    fun whenCanCreateGroupIsTrue_showCreateGroupButton() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS.copy(canCreateGroup = true)),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_NEW_GROUP_TEST_TAG_PREFIX + ACCOUNT.name).assertIsDisplayed()
    }

    @Test
    fun whenCanCreateGroupIsFalse_doNotShowCreateGroupButton() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS.copy(canCreateGroup = false)),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_NEW_GROUP_TEST_TAG_PREFIX + ACCOUNT.name).assertIsNotDisplayed()
    }

    @Test
    fun whenThereAreGroups_showGroupsWithNameAndCount() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_GROUP_TEST_TAG_PREFIX + GROUP.id).assertIsDisplayed()
        onNodeWithText(GROUP.name).assertIsDisplayed()
        onNodeWithText(GROUP.summaryCount.toString()).assertIsDisplayed()
    }

    @Test
    fun onNewGroupClick_sendNewClickedAction() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_NEW_GROUP_TEST_TAG_PREFIX + ACCOUNT.name).performClick()

        verify { screenModel.onAction(Action.NewClicked(ACCOUNT)) }
    }

    @Test
    fun onGroupClick_sendGroupClickedAction() = runComposeUiTest {
        withState(
            State.WithAccounts(
                groups = persistentListOf(ACCOUNT_GROUPS),
                showAccountHeaders = true,
            ),
        )
        setScreenContent()

        onNodeWithTag(GROUPS_GROUP_TEST_TAG_PREFIX + GROUP.id).performClick()

        verify { screenModel.onAction(Action.GroupClicked(GROUP)) }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            GroupsScreen(
                effectHandler = mockk<GroupsEffectHandler>(relaxed = true),
                screenModel = screenModel,
            )
        }
    }

    private fun withState(state: State) {
        every { screenModel.uiState } returns MutableStateFlow(state)
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
    }
}
