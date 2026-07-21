package com.android.contacts.sim

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.android.contacts.tests.AccountUiModelFactory
import com.android.contacts.tests.SimContactFactory
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.simimport.screen.SimImportEffectHandler
import com.android.contacts.ui.simimport.screen.SimImportScreen
import com.android.contacts.ui.simimport.screen.SimImportScreenModel
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_ACCOUNT_PICKER_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_DESELECT_ALL_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_IMPORT_BUTTON_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_SELECT_ALL_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SimImportAction
import com.android.contacts.ui.simimport.screen.model.SimImportUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SimImportScreenTest {

    private val fakeUiStateFlow = MutableStateFlow(SimImportUiState())
    private lateinit var screenModel: SimImportScreenModel
    private lateinit var effectHandler: SimImportEffectHandler

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun showCurrentAccount() = runComposeUiTest {
        val account = AccountUiModelFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = persistentListOf(account),
            currentAccount = account,
        )

        setScreenContent()

        onNodeWithText(account.name!!).assertIsDisplayed()
    }

    @Test
    fun pickAnotherAccount() = runComposeUiTest {
        val account1 = AccountUiModelFactory.build(name = "First")
        val account2 = AccountUiModelFactory.build(name = "Second")
        fakeUiStateFlow.value = SimImportUiState(
            accounts = persistentListOf(account1, account2),
            currentAccount = account1,
        )

        setScreenContent()

        onNodeWithText(account1.name!!).assertIsDisplayed()
        onNodeWithTag(SIM_IMPORT_ACCOUNT_PICKER_TEST_TAG).performClick()
        onNode(
            hasText(account2.name!!)
                .and(hasTestTag(SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM_TEST_TAG)),
        ).performClick()
        verify { screenModel.onAction(SimImportAction.AccountChanged(account2)) }
    }

    @Test
    fun showContactToImport() = runComposeUiTest {
        val account = AccountUiModelFactory.build()
        val contact = SimContactFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = persistentListOf(account),
            currentAccount = account,
            contactsToImport = persistentListOf(SelectableItem(contact, false)),
            contactsAlreadyImported = persistentListOf(),
        )

        setScreenContent()

        onNodeWithTag(SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE_TEST_TAG).assertIsDisplayed()
        onNodeWithText(contact.name).assertIsDisplayed()
    }

    @Test
    fun clickContact() = runComposeUiTest {
        val account = AccountUiModelFactory.build()
        val contact = SimContactFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = persistentListOf(account),
            currentAccount = account,
            contactsToImport = persistentListOf(SelectableItem(contact, false)),
            contactsAlreadyImported = persistentListOf(),
        )

        setScreenContent()

        onNodeWithText(contact.name).performClick()
        verify { screenModel.onAction(SimImportAction.ContactSelectionChanged(contact, true)) }
    }

    @Test
    fun checkTopBarActionsCanBeDisabled() = runComposeUiTest {
        val account = AccountUiModelFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = persistentListOf(account),
            currentAccount = account,
            contactsToImport = persistentListOf(),
            contactsAlreadyImported = persistentListOf(),
        )

        setScreenContent()

        onNodeWithTag(SIM_IMPORT_IMPORT_BUTTON_TEST_TAG).assertIsNotEnabled()
        onNodeWithTag(SIM_IMPORT_SELECT_ALL_TEST_TAG).assertIsNotEnabled()
        onNodeWithTag(SIM_IMPORT_DESELECT_ALL_TEST_TAG).assertIsNotEnabled()
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            SimImportScreen(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }
}
