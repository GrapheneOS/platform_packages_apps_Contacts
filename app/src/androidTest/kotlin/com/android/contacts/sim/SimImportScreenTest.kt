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
import com.android.contacts.tests.AccountInfoFactory
import com.android.contacts.tests.SimContactFactory
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.core.AppScaffold
import com.android.contacts.ui.simimport.common.TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER
import com.android.contacts.ui.simimport.common.TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM
import com.android.contacts.ui.simimport.screen.SimImportEffectHandler
import com.android.contacts.ui.simimport.screen.SimImportScreen
import com.android.contacts.ui.simimport.screen.SimImportScreenModel
import com.android.contacts.ui.simimport.screen.TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE
import com.android.contacts.ui.simimport.screen.TEST_TAG_SIM_IMPORT_DESELECT_ALL
import com.android.contacts.ui.simimport.screen.TEST_TAG_SIM_IMPORT_IMPORT_BUTTON
import com.android.contacts.ui.simimport.screen.TEST_TAG_SIM_IMPORT_SELECT_ALL
import com.android.contacts.ui.simimport.screen.model.SimImportAction
import com.android.contacts.ui.simimport.screen.model.SimImportUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        val account = AccountInfoFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = listOf(account),
            currentAccount = account,
        )

        setScreenContent()

        onNodeWithText(account.nameLabel.toString()).assertIsDisplayed()
    }

    @Test
    fun pickAnotherAccount() = runComposeUiTest {
        val account1 = AccountInfoFactory.build(name = "First")
        val account2 = AccountInfoFactory.build(name = "Second")
        fakeUiStateFlow.value = SimImportUiState(
            accounts = listOf(account1, account2),
            currentAccount = account1,
        )

        setScreenContent()

        onNodeWithText(account1.nameLabel.toString()).assertIsDisplayed()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER).performClick()
        onNode(
            hasText(account2.nameLabel.toString())
                .and(hasTestTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM)),
        ).performClick()
        verify { screenModel.onAction(SimImportAction.AccountChanged(account2)) }
    }

    @Test
    fun showContactToImport() = runComposeUiTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = listOf(account),
            currentAccount = account,
            contactsToImport = listOf(SelectableItem(contact, false)),
        )

        setScreenContent()

        onNodeWithTag(TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE).assertIsDisplayed()
        onNodeWithText(contact.name).assertIsDisplayed()
    }

    @Test
    fun clickContact() = runComposeUiTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = listOf(account),
            currentAccount = account,
            contactsToImport = listOf(SelectableItem(contact, false)),
        )

        setScreenContent()

        onNodeWithText(contact.name).performClick()
        verify { screenModel.onAction(SimImportAction.ContactClicked(contact)) }
    }

    @Test
    fun checkTopBarActionsCanBeDisabled() = runComposeUiTest {
        val account = AccountInfoFactory.build()
        fakeUiStateFlow.value = SimImportUiState(
            accounts = listOf(account),
            currentAccount = account,
            contactsToImport = emptyList(),
        )

        setScreenContent()

        onNodeWithTag(TEST_TAG_SIM_IMPORT_IMPORT_BUTTON).assertIsNotEnabled()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_SELECT_ALL).assertIsNotEnabled()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_DESELECT_ALL).assertIsNotEnabled()
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            AppScaffold {
                SimImportScreen(
                    effectHandler = effectHandler,
                    screenModel = screenModel,
                )
            }
        }
    }
}
