package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OrganizationSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersCompanyAndTitleFields() {
        setContent(OrganizationFieldState())
        composeTestRule.onNodeWithTag(TestTags.ORG_COMPANY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ORG_TITLE).assertIsDisplayed()
    }

    @Test
    fun typeInCompany_dispatchesUpdateCompany() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ORG_COMPANY).performTextInput("Acme")
        assertIs<ContactCreationAction.UpdateCompany>(capturedActions.last())
        assertEquals(
            "Acme",
            (capturedActions.last() as ContactCreationAction.UpdateCompany).value,
        )
    }

    @Test
    fun typeInTitle_dispatchesUpdateJobTitle() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ORG_TITLE).performTextInput("CTO")
        assertIs<ContactCreationAction.UpdateJobTitle>(capturedActions.last())
        assertEquals(
            "CTO",
            (capturedActions.last() as ContactCreationAction.UpdateJobTitle).value,
        )
    }

    @Test
    fun preFilledState_rendersValues() {
        setContent(
            OrganizationFieldState(company = "Google", title = "SWE"),
        )
        composeTestRule.onNodeWithTag(TestTags.ORG_COMPANY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ORG_TITLE).assertIsDisplayed()
    }

    private fun setContent(
        organization: OrganizationFieldState = OrganizationFieldState(),
    ) {
        composeTestRule.setContent {
            AppTheme {
                OrganizationSectionContent(
                    organization = organization,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
