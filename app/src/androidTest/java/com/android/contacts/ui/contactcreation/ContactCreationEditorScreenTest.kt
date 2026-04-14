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
    fun initialState_showsSaveButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun initialState_showsBackButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON).assertIsDisplayed()
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
    fun initialState_showsAccountChip() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ACCOUNT_CHIP).assertIsDisplayed()
    }

    @Test
    fun tapSave_dispatchesSaveAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    @Test
    fun tapBack_dispatchesNavigateBackAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON).performClick()
        assertEquals(ContactCreationAction.NavigateBack, capturedActions.last())
    }

    @Test
    fun savingState_disablesSaveButton() {
        setContent(state = ContactCreationUiState(isSaving = true))
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun notSavingState_enablesSaveButton() {
        setContent(state = ContactCreationUiState(isSaving = false))
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).assertIsEnabled()
    }

    private fun setContent(state: ContactCreationUiState = ContactCreationUiState()) {
        composeTestRule.setContent {
            AppTheme {
                ContactCreationEditorScreen(
                    uiState = state,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
