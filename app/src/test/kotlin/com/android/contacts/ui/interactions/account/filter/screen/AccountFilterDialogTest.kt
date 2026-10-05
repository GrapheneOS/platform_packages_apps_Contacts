package com.android.contacts.ui.interactions.account.filter.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_ITEM_ACCOUNT_TEST_TAG_PREFIX
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_ITEM_ALL_TEST_TAG
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_LOADING_TEST_TAG
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterUiState as State
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class AccountFilterDialogTest {

    private val screenModel = mockk<AccountFilterScreenModel>(relaxed = true)

    @Test
    fun whenLoading_showLoading() = runComposeUiTest {
        withState(State.Loading)
        setScreenContent()

        onNodeWithTag(ACCOUNT_FILTER_LOADING_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun whenRead_showItems() = runComposeUiTest {
        withState(State.Ready(ITEMS))
        setScreenContent()

        onNodeWithTag(ACCOUNT_FILTER_LOADING_TEST_TAG).assertIsNotDisplayed()

        onNodeWithTag(ACCOUNT_FILTER_ITEM_ALL_TEST_TAG).assertIsDisplayed()
        onNodeWithText("30").assertIsDisplayed()

        onNodeWithTag(ACCOUNT_FILTER_ITEM_ACCOUNT_TEST_TAG_PREFIX + "Account 1")
            .assertIsDisplayed()
            .assertIsSelected()
        onNodeWithText("10").assertIsDisplayed()

        onNodeWithTag(ACCOUNT_FILTER_ITEM_ACCOUNT_TEST_TAG_PREFIX + "Account 2")
            .assertIsDisplayed()
        onNodeWithText("Device").assertIsDisplayed()
        onNodeWithText("20").assertIsDisplayed()
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            AccountFilterDialog(
                effectHandler = mockk<AccountFilterEffectHandler>(relaxed = true),
                screenModel = screenModel,
            )
        }
    }

    private fun withState(state: State) {
        every { screenModel.uiState } returns MutableStateFlow(state)
    }

    companion object {
        private val ALL_ITEM = AccountFilterItem.All(contactsCount = 30)
        private val ACCOUNT_ITEM_1 = AccountFilterItem.One(
            name = "Account 1",
            contactsCount = 10,
            filter = ContactsAccountFilter.One(AccountModel("Account 1")),
        )
        private val ACCOUNT_ITEM_2 = AccountFilterItem.One(
            name = "Account 2",
            description = "Device",
            contactsCount = 20,
            filter = ContactsAccountFilter.One(AccountModel("Account 2")),
        )
        private val ITEMS = persistentListOf<SelectableItem<AccountFilterItem>>(
            SelectableItem(ALL_ITEM, false),
            SelectableItem(ACCOUNT_ITEM_1, true),
            SelectableItem(ACCOUNT_ITEM_2, false),
        )
    }
}
