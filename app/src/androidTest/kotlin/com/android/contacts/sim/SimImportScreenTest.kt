package com.android.contacts.sim

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.android.contacts.sim.ui.SelectableItem
import com.android.contacts.sim.ui.SimImportScreen
import com.android.contacts.sim.ui.SimImportViewModel
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_DESELECT_ALL
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_IMPORT_BUTTON
import com.android.contacts.sim.ui.TEST_TAG_SIM_IMPORT_SELECT_ALL
import com.android.contacts.tests.AccountInfoFactory
import com.android.contacts.tests.SimContactFactory
import com.android.contacts.ui.core.AppScaffold
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SimImportScreenTest {

    @Test
    fun showCurrentAccount() = runComposeUiTest {
        val account = AccountInfoFactory.build()

        setContent {
            AppScaffold {
                SimImportScreen(
                    state = SimImportViewModel.State(
                        accounts = listOf(account),
                        currentAccount = account,
                    ),
                    onEvent = {},
                    onClose = {},
                )
            }
        }

        onNodeWithText(account.nameLabel.toString()).assertIsDisplayed()
    }

    @Test
    fun pickAnotherAccount() = runComposeUiTest {
        val account1 = AccountInfoFactory.build(name = "First")
        val account2 = AccountInfoFactory.build(name = "Second")
        var lastEvent: SimImportViewModel.Event? = null

        setContent {
            AppScaffold {
                SimImportScreen(
                    state = SimImportViewModel.State(
                        accounts = listOf(account1, account2),
                        currentAccount = account1,
                    ),
                    onEvent = { lastEvent = it },
                    onClose = {},
                )
            }
        }

        onNodeWithText(account1.nameLabel.toString()).assertIsDisplayed()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER).performClick()
        onNode(
            hasText(account2.nameLabel.toString())
                .and(hasTestTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM)),
        ).performClick()
        assertEquals(SimImportViewModel.Event.AccountChanged(account2), lastEvent)
    }

    @Test
    fun showContactToImport() = runComposeUiTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()

        setContent {
            AppScaffold {
                SimImportScreen(
                    state = SimImportViewModel.State(
                        accounts = listOf(account),
                        currentAccount = account,
                        contactsToImport = listOf(SelectableItem(contact, false)),
                    ),
                    onEvent = {},
                    onClose = {},
                )
            }
        }

        onNodeWithTag(TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE).assertIsDisplayed()
        onNodeWithText(contact.name).assertIsDisplayed()
    }

    @Test
    fun clickContact() = runComposeUiTest {
        val account = AccountInfoFactory.build()
        val contact = SimContactFactory.build()
        var lastEvent: SimImportViewModel.Event? = null

        setContent {
            AppScaffold {
                SimImportScreen(
                    state = SimImportViewModel.State(
                        accounts = listOf(account),
                        currentAccount = account,
                        contactsToImport = listOf(SelectableItem(contact, false)),
                    ),
                    onEvent = { lastEvent = it },
                    onClose = {},
                )
            }
        }

        onNodeWithText(contact.name).performClick()
        assertEquals(SimImportViewModel.Event.ContactClicked(contact), lastEvent)
    }

    @Test
    fun checkTopBarActionsCanBeDisabled() = runComposeUiTest {
        val account = AccountInfoFactory.build()

        setContent {
            AppScaffold {
                SimImportScreen(
                    state = SimImportViewModel.State(
                        accounts = listOf(account),
                        currentAccount = account,
                        contactsToImport = emptyList(),
                    ),
                    onEvent = {},
                    onClose = {},
                )
            }
        }

        onNodeWithTag(TEST_TAG_SIM_IMPORT_IMPORT_BUTTON).assertIsNotEnabled()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_SELECT_ALL).assertIsNotEnabled()
        onNodeWithTag(TEST_TAG_SIM_IMPORT_DESELECT_ALL).assertIsNotEnabled()
    }
}
