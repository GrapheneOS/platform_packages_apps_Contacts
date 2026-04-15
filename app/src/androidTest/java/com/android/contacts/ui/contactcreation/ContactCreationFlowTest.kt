package com.android.contacts.ui.contactcreation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end flow tests exercising the full [ContactCreationEditorScreen].
 *
 * Uses [createAndroidComposeRule] with [ComponentActivity] + the screen composable
 * directly (avoids Hilt wiring for the Activity). Actions are captured via lambda
 * to verify the full UI -> action pipeline.
 */
class ContactCreationFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    // --- 1. Basic save flow ---

    @Test
    fun createBasicContact_endToEnd() {
        setContent()
        // Type first name
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).performTextInput("John")
        assertTrue(
            capturedActions.any { it is ContactCreationAction.UpdateFirstName },
        )

        // Type phone
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).performTextInput("555-0100")
        assertTrue(
            capturedActions.any { it is ContactCreationAction.UpdatePhone },
        )

        // Tap save
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    // --- 2. All fields save flow ---

    @Test
    fun createWithAllFields_endToEnd() {
        val state = TestFactory.fullState()
        setContent(state = state)

        // Verify all major sections are rendered
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.emailField(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.addressStreet(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ORG_COMPANY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ORG_TITLE).assertIsDisplayed()

        // Tap save
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    // --- 3. Cancel with discard flow ---

    @Test
    fun cancelWithDiscard_endToEnd() {
        setContent(state = ContactCreationUiState(showDiscardDialog = true))

        // Discard dialog should be visible
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG).assertIsDisplayed()

        // Tap discard (confirm button)
        composeTestRule.onNodeWithTag(TestTags.DISCARD_DIALOG_CONFIRM).performClick()
        assertEquals(ContactCreationAction.ConfirmDiscard, capturedActions.last())
    }

    // --- 4. Intent extras pre-fill ---

    @Test
    fun intentExtras_preFill_endToEnd() {
        // Simulate pre-filled state (as Activity.applyIntentExtras would produce)
        val preFilled = ContactCreationUiState(
            nameState = NameState(first = "Jane"),
            phoneNumbers = listOf(PhoneFieldState(id = "p1", number = "555-1234")),
        )
        setContent(state = preFilled)

        // Fields should be displayed with pre-filled data
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).assertIsDisplayed()

        // Save
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    // --- 5. Zero-account local contact ---

    @Test
    fun zeroAccount_localContact_endToEnd() {
        // No account selected -> chip shows "Device"
        val state = ContactCreationUiState(
            selectedAccount = null,
            accountName = null,
        )
        setContent(state = state)

        // Account chip should be visible (showing "Device" text)
        composeTestRule.onNodeWithTag(TestTags.ACCOUNT_CHIP).assertIsDisplayed()

        // Type a name and save
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).performTextInput("Local")
        composeTestRule.onNodeWithTag(TestTags.SAVE_TEXT_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    // --- Helper ---

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
