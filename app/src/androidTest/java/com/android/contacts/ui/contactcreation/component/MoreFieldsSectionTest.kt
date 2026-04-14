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
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MoreFieldsSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersMoreFieldsToggle() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.MORE_FIELDS_TOGGLE).assertIsDisplayed()
    }

    @Test
    fun tapToggle_dispatchesToggleMoreFieldsAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.MORE_FIELDS_TOGGLE).performClick()
        assertEquals(ContactCreationAction.ToggleMoreFields, capturedActions.last())
    }

    @Test
    fun whenExpanded_showsNicknameField() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.NICKNAME_FIELD).assertIsDisplayed()
    }

    @Test
    fun whenExpanded_showsNoteField() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.NOTE_FIELD).assertIsDisplayed()
    }

    @Test
    fun whenExpanded_showsSipField() {
        setContent(isExpanded = true, showSipField = true)
        composeTestRule.onNodeWithTag(TestTags.SIP_FIELD).assertIsDisplayed()
    }

    @Test
    fun whenExpanded_hiddenSipField_doesNotShow() {
        setContent(isExpanded = true, showSipField = false)
        composeTestRule.onNodeWithTag(TestTags.SIP_FIELD).assertDoesNotExist()
    }

    @Test
    fun typeInNickname_dispatchesUpdateNicknameAction() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.NICKNAME_FIELD).performTextInput("Johnny")
        assertIs<ContactCreationAction.UpdateNickname>(capturedActions.last())
    }

    @Test
    fun typeInNote_dispatchesUpdateNoteAction() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.NOTE_FIELD).performTextInput("A note")
        assertIs<ContactCreationAction.UpdateNote>(capturedActions.last())
    }

    @Test
    fun typeInSip_dispatchesUpdateSipAddressAction() {
        setContent(isExpanded = true, showSipField = true)
        composeTestRule.onNodeWithTag(TestTags.SIP_FIELD).performTextInput("sip:user@voip")
        assertIs<ContactCreationAction.UpdateSipAddress>(capturedActions.last())
    }

    @Test
    fun whenExpanded_showsEventAddButton() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.EVENT_ADD).assertIsDisplayed()
    }

    @Test
    fun tapAddEvent_dispatchesAddEventAction() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.EVENT_ADD).performClick()
        assertEquals(ContactCreationAction.AddEvent, capturedActions.last())
    }

    @Test
    fun whenExpanded_showsRelationAddButton() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.RELATION_ADD).assertIsDisplayed()
    }

    @Test
    fun whenExpanded_showsImAddButton() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.IM_ADD).assertIsDisplayed()
    }

    @Test
    fun whenExpanded_showsWebsiteAddButton() {
        setContent(isExpanded = true)
        composeTestRule.onNodeWithTag(TestTags.WEBSITE_ADD).assertIsDisplayed()
    }

    @Test
    fun eventFieldRendered_whenPresent() {
        setContent(
            isExpanded = true,
            events = listOf(EventFieldState(id = "e1", startDate = "2020-01-01")),
        )
        composeTestRule.onNodeWithTag(TestTags.eventField(0)).assertIsDisplayed()
    }

    @Test
    fun typeInEvent_dispatchesUpdateEventAction() {
        setContent(
            isExpanded = true,
            events = listOf(EventFieldState(id = "e1")),
        )
        composeTestRule.onNodeWithTag(TestTags.eventField(0)).performTextInput("2020-01-01")
        assertIs<ContactCreationAction.UpdateEvent>(capturedActions.last())
    }

    @Test
    fun tapDeleteEvent_dispatchesRemoveEventAction() {
        setContent(
            isExpanded = true,
            events = listOf(EventFieldState(id = "e1")),
        )
        composeTestRule.onNodeWithTag(TestTags.eventDelete(0)).performClick()
        assertIs<ContactCreationAction.RemoveEvent>(capturedActions.last())
    }

    @Test
    fun relationFieldRendered_whenPresent() {
        setContent(
            isExpanded = true,
            relations = listOf(RelationFieldState(id = "r1")),
        )
        composeTestRule.onNodeWithTag(TestTags.relationField(0)).assertIsDisplayed()
    }

    @Test
    fun imFieldRendered_whenPresent() {
        setContent(
            isExpanded = true,
            imAccounts = listOf(ImFieldState(id = "im1")),
        )
        composeTestRule.onNodeWithTag(TestTags.imField(0)).assertIsDisplayed()
    }

    @Test
    fun websiteFieldRendered_whenPresent() {
        setContent(
            isExpanded = true,
            websites = listOf(WebsiteFieldState(id = "w1")),
        )
        composeTestRule.onNodeWithTag(TestTags.websiteField(0)).assertIsDisplayed()
    }

    @Suppress("LongParameterList")
    private fun setContent(
        isExpanded: Boolean = false,
        events: List<EventFieldState> = emptyList(),
        relations: List<RelationFieldState> = emptyList(),
        imAccounts: List<ImFieldState> = emptyList(),
        websites: List<WebsiteFieldState> = emptyList(),
        note: String = "",
        nickname: String = "",
        sipAddress: String = "",
        showSipField: Boolean = true,
    ) {
        composeTestRule.setContent {
            AppTheme {
                LazyColumn {
                    moreFieldsSection(
                        isExpanded = isExpanded,
                        events = events,
                        relations = relations,
                        imAccounts = imAccounts,
                        websites = websites,
                        note = note,
                        nickname = nickname,
                        sipAddress = sipAddress,
                        showSipField = showSipField,
                        onAction = { capturedActions.add(it) },
                    )
                }
            }
        }
    }
}
