package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NameSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersFirstNameField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).assertIsDisplayed()
    }

    @Test
    fun rendersLastNameField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_LAST).assertIsDisplayed()
    }

    @Test
    fun typeFirstName_dispatchesUpdateFirstName() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).performTextInput("John")
        assertIs<ContactCreationAction.UpdateFirstName>(capturedActions.last())
    }

    @Test
    fun typeLastName_dispatchesUpdateLastName() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_LAST).performTextInput("Doe")
        assertIs<ContactCreationAction.UpdateLastName>(capturedActions.last())
    }

    private fun setContent(nameState: NameState = NameState()) {
        composeTestRule.setContent {
            AppTheme {
                NameSectionContent(
                    nameState = nameState,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
