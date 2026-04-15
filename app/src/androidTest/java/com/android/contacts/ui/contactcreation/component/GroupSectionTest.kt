package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.GroupInfo
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GroupSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun noAvailableGroups_sectionNotShown() {
        setContent(availableGroups = emptyList())
        composeTestRule.onNodeWithTag(TestTags.GROUP_SECTION).assertDoesNotExist()
    }

    @Test
    fun availableGroups_showsGroupSection() {
        setContent(
            availableGroups = listOf(GroupInfo(groupId = 1L, title = "Friends")),
        )
        composeTestRule.onNodeWithTag(TestTags.GROUP_SECTION).assertIsDisplayed()
    }

    @Test
    fun rendersCheckboxForEachGroup() {
        setContent(
            availableGroups = listOf(
                GroupInfo(groupId = 1L, title = "Friends"),
                GroupInfo(groupId = 2L, title = "Family"),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.groupCheckbox(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.groupCheckbox(1)).assertIsDisplayed()
    }

    @Test
    fun selectedGroup_showsChecked() {
        setContent(
            availableGroups = listOf(GroupInfo(groupId = 1L, title = "Friends")),
            selectedGroups = listOf(GroupFieldState(groupId = 1L, title = "Friends")),
        )
        composeTestRule.onNodeWithTag(TestTags.groupCheckbox(0)).assertIsOn()
    }

    @Test
    fun unselectedGroup_showsUnchecked() {
        setContent(
            availableGroups = listOf(GroupInfo(groupId = 1L, title = "Friends")),
            selectedGroups = emptyList(),
        )
        composeTestRule.onNodeWithTag(TestTags.groupCheckbox(0)).assertIsOff()
    }

    @Test
    fun tapCheckbox_dispatchesToggleGroupAction() {
        setContent(
            availableGroups = listOf(GroupInfo(groupId = 42L, title = "Friends")),
        )
        composeTestRule.onNodeWithTag(TestTags.groupCheckbox(0)).performClick()
        val action = capturedActions.last()
        assertIs<ContactCreationAction.ToggleGroup>(action)
        assertEquals(42L, action.groupId)
        assertEquals("Friends", action.title)
    }

    private fun setContent(
        availableGroups: List<GroupInfo> = emptyList(),
        selectedGroups: List<GroupFieldState> = emptyList(),
    ) {
        composeTestRule.setContent {
            AppTheme {
                GroupSectionContent(
                    availableGroups = availableGroups,
                    selectedGroups = selectedGroups,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
