package com.android.contacts.ui.contactcreation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactCreationEditorScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun initialState_showsSaveTextButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun initialState_showsCloseButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.CLOSE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun initialState_showsNameField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).assertIsDisplayed()
    }

    @Test
    fun initialState_showsPhoneField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).assertIsDisplayed()
    }

    @Test
    fun initialState_showsEmailField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.emailField(0)).assertIsDisplayed()
    }

    @Test
    fun tapSave_dispatchesSaveAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    @Test
    fun tapClose_dispatchesNavigateBackAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.CLOSE_BUTTON).performClick()
        assertEquals(ContactCreationAction.NavigateBack, capturedActions.last())
    }

    @Test
    fun savingState_disablesSaveButton() {
        setContent(state = ContactCreationUiState(isSaving = true))
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun notSavingState_enablesSaveButton() {
        setContent(state = ContactCreationUiState(isSaving = false))
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).assertIsEnabled()
    }

    // --- Discard dialog ---

    @Test
    fun discardDialog_rendersWhenShowDiscardDialogTrue() {
        setContent(state = ContactCreationUiState(showDiscardDialog = true))
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG).assertIsDisplayed()
    }

    @Test
    fun discardDialog_notRenderedByDefault() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG).assertDoesNotExist()
    }

    @Test
    fun discardDialog_confirmDispatchesConfirmDiscard() {
        setContent(state = ContactCreationUiState(showDiscardDialog = true))
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG_CONFIRM).performClick()
        assertEquals(ContactCreationAction.ConfirmDiscard, capturedActions.last())
    }

    @Test
    fun discardDialog_dismissDispatchesDismissDiscardDialog() {
        setContent(state = ContactCreationUiState(showDiscardDialog = true))
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG_DISMISS).performClick()
        assertEquals(ContactCreationAction.DismissDiscardDialog, capturedActions.last())
    }

    // --- Add more info chip grid ---

    @Test
    fun addMoreInfoSection_showsWhenChipsAvailable() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ADD_MORE_INFO_SECTION).assertIsDisplayed()
    }

    @Test
    fun addMoreInfoSection_addressChipAddsAddress() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.addMoreInfoChip("address")).performClick()
        assertEquals(ContactCreationAction.AddAddress, capturedActions.last())
    }

    @Test
    fun addMoreInfoSection_orgChipShowsOrganization() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.addMoreInfoChip("organization")).performClick()
        assertEquals(ContactCreationAction.ShowOrganization, capturedActions.last())
    }

    @Test
    fun addMoreInfoSection_noteChipShowsNote() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.addMoreInfoChip("note")).performClick()
        assertEquals(ContactCreationAction.ShowNote, capturedActions.last())
    }

    private fun setContent(state: ContactCreationUiState = ContactCreationUiState()) {
        composeTestRule.setContent {
            AppTheme {
                ContactCreationEditorScreen(
                    uiState = state,
                    accounts = emptyList(),
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
