package com.android.contacts.ui.editor.springboard.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.tests.RawContactUiModelFactory
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_ADD_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_LINKED_CONTACTS_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_PICK_RAW_CONTACT_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_RAW_CONTACT_TEST_TAG_PREFIX
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CANCEL_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CONFIRMATION_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CONFIRM_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_UNLINK_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardAction as Action
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardUiState as State
import kotlinx.collections.immutable.persistentListOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactEditorSpringBoardScreenTest {

    private val actions = mutableListOf<Action>()

    @After
    fun tearDown() {
        actions.clear()
    }

    @Test
    fun showPickContactToEdit() = runComposeUiTest {
        val contact = RawContactUiModelFactory.build(
            id = 1L,
            displayName = "Name",
            accountLabel = "Account",
        )
        setScreenContent(
            State.ShowPickContactToEdit(contacts = persistentListOf(contact)),
        )

        waitForIdle()

        onNodeWithTag(CONTACT_EDITOR_SB_PICK_RAW_CONTACT_TEST_TAG)
            .assertIsDisplayed()
        onNodeWithText(contact.displayName).assertIsDisplayed()
        onNodeWithText(contact.accountLabel!!).assertIsDisplayed()

        onNodeWithTag(CONTACT_EDITOR_SB_RAW_CONTACT_TEST_TAG_PREFIX + contact.id)
            .performClick()

        assertEquals(
            Action.ContactClicked(contact.id),
            actions.last(),
        )
    }

    @Test
    fun showLinkedContacts() = runComposeUiTest {
        val contact = RawContactUiModelFactory.build(
            id = 1L,
            displayName = "Name",
            accountLabel = "Account",
        )
        setScreenContent(
            State.ShowLinkedContacts(
                contacts = persistentListOf(contact),
                isUserProfile = false,
            ),
        )

        waitForIdle()

        onNodeWithTag(CONTACT_EDITOR_SB_LINKED_CONTACTS_TEST_TAG).assertIsDisplayed()
        onNodeWithText(contact.displayName).assertIsDisplayed()
        onNodeWithText(contact.accountLabel!!).assertIsDisplayed()

        onNodeWithTag(CONTACT_EDITOR_SB_RAW_CONTACT_TEST_TAG_PREFIX + contact.id)
            .performClick()
        assertEquals(Action.ContactClicked(contact.id), actions.last())

        onNodeWithTag(CONTACT_EDITOR_SB_ADD_BUTTON_TEST_TAG).performClick()
        assertEquals(Action.AddClicked, actions.last())

        onNodeWithTag(CONTACT_EDITOR_SB_UNLINK_BUTTON_TEST_TAG).performClick()
        assertEquals(Action.UnlinkClicked, actions.last())
    }

    @Test
    fun splitConfirmation() = runComposeUiTest {
        setScreenContent(State.ShowUnlinkConfirmation)

        waitForIdle()

        onNodeWithTag(CONTACT_EDITOR_SB_SPLIT_CONFIRMATION_TEST_TAG)
            .assertIsDisplayed()

        onNodeWithTag(CONTACT_EDITOR_SB_SPLIT_CONFIRM_BUTTON_TEST_TAG)
            .performClick()
        assertEquals(Action.UnlinkConfirmed, actions.last())

        onNodeWithTag(CONTACT_EDITOR_SB_SPLIT_CANCEL_BUTTON_TEST_TAG)
            .performClick()
        assertEquals(Action.UnlinkDismissed, actions.last())
    }

    private fun ComposeUiTest.setScreenContent(state: State) {
        setContent {
            ContactEditorSpringBoardContent(
                uiState = state,
                onAction = { actions.add(it) },
            )
        }
    }
}
