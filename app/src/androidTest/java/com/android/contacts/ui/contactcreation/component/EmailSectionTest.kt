package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmailSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersEmailField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.emailField(0)).assertIsDisplayed()
    }

    @Test
    fun rendersAddEmailButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.EMAIL_ADD).assertIsDisplayed()
    }

    @Test
    fun typeInEmail_dispatchesUpdateEmail() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.emailField(0)).performTextInput("a@b.com")
        assertIs<ContactCreationAction.UpdateEmail>(capturedActions.last())
    }

    @Test
    fun tapAddEmail_dispatchesAddEmailAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.EMAIL_ADD).performClick()
        assertEquals(ContactCreationAction.AddEmail, capturedActions.last())
    }

    @Test
    fun multipleEmails_showsDeleteButtons() {
        val emails = listOf(
            EmailFieldState(id = "1", address = "a@b.com"),
            EmailFieldState(id = "2", address = "c@d.com"),
        )
        setContent(emails = emails)
        composeTestRule.onNodeWithTag(TestTags.emailDelete(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.emailDelete(1)).assertIsDisplayed()
    }

    @Test
    fun tapDeleteEmail_dispatchesRemoveEmailAction() {
        val emails = listOf(
            EmailFieldState(id = "1", address = "a@b.com"),
            EmailFieldState(id = "2", address = "c@d.com"),
        )
        setContent(emails = emails)
        composeTestRule.onNodeWithTag(TestTags.emailDelete(1)).performClick()
        assertIs<ContactCreationAction.RemoveEmail>(capturedActions.last())
    }

    @Test
    fun rendersEmailTypeSelector() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.emailType(0)).assertIsDisplayed()
    }

    @Test
    fun tapEmailType_showsDropdownMenu() {
        val email = EmailFieldState(id = "1", address = "a@b.com", type = EmailType.Home)
        setContent(emails = listOf(email))
        composeTestRule.onNodeWithTag(TestTags.emailType(0)).performClick()
        composeTestRule.onNodeWithTag(TestTags.emailType(0)).assertIsDisplayed()
    }

    private fun setContent(emails: List<EmailFieldState> = listOf(EmailFieldState())) {
        composeTestRule.setContent {
            AppTheme {
                LazyColumn {
                    emailSection(
                        emails = emails,
                        onAction = { capturedActions.add(it) },
                    )
                }
            }
        }
    }
}
