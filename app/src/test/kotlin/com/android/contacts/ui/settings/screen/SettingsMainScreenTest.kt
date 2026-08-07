package com.android.contacts.ui.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.ui.settings.screen.model.SETTINGS_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_ITEM_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_SECTION_HEADER_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsGroupId
import com.android.contacts.ui.settings.screen.model.SettingsGroupUiModel
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsItemUiModel
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SettingsMainScreenTest {

    @Test
    fun showsEveryGroupAndItem() = runComposeUiTest {
        setContent {
            MainScreen()
        }

        onNodeWithTag(SETTINGS_GROUP_TEST_TAG_PREFIX + SettingsGroupId.PROFILE.name)
            .assertIsDisplayed()
        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name)
            .assertIsDisplayed()
        onNodeWithText(MY_INFO_SUMMARY).assertIsDisplayed()
        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.SORT_ORDER.name)
            .assertIsDisplayed()
    }

    @Test
    fun whenGroupHasATitle_showsItAsSectionHeading() = runComposeUiTest {
        setContent {
            MainScreen()
        }

        onNodeWithTag(SETTINGS_SECTION_HEADER_TEST_TAG_PREFIX + SettingsGroupId.DISPLAY.name)
            .assertTextEquals(DISPLAY_SECTION_TITLE)
    }

    @Test
    fun whenGroupHasNoTitle_showsNoSectionHeading() = runComposeUiTest {
        setContent {
            MainScreen()
        }

        onNodeWithTag(SETTINGS_SECTION_HEADER_TEST_TAG_PREFIX + SettingsGroupId.PROFILE.name)
            .assertDoesNotExist()
    }

    @Test
    fun whenThereAreNoGroups_showsNoItems() = runComposeUiTest {
        setContent {
            MainScreen(uiState = SettingsUiState())
        }

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name)
            .assertDoesNotExist()
    }

    @Test
    fun whenPlainItemIsClicked_reportsTheClick() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContent {
            MainScreen(onAction = actions::add)
        }

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name).performClick()

        assertEquals(listOf(Action.ItemClicked(SettingsItemId.MY_INFO)), actions)
    }

    @Test
    fun whenAboutIsClicked_navigatesToAbout() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        var aboutClicks = 0
        setContent {
            MainScreen(onAction = actions::add, onNavigateToAbout = { aboutClicks++ })
        }

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.ABOUT.name).performClick()

        assertEquals(1, aboutClicks)
        assertEquals(emptyList<Action>(), actions)
    }

    @Test
    fun whenDisplayOptionIsClicked_opensItsDialogWithoutReportingAnAction() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContent {
            MainScreen(onAction = actions::add)
        }

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.SORT_ORDER.name).performClick()

        onNodeWithTag(SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG).assertIsDisplayed()
        assertEquals(emptyList<Action>(), actions)
    }

    @Test
    fun whenSortOrderIsSelected_reportsItAndClosesTheDialog() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContent {
            MainScreen(onAction = actions::add)
        }
        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.SORT_ORDER.name).performClick()

        onNodeWithText(FAMILY_NAME_FIRST_LABEL).performClick()

        assertEquals(listOf(Action.SortOrderSelected(SortOrder.FAMILY_NAME_FIRST)), actions)
        onNodeWithTag(SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenBackIsClicked_navigatesBack() = runComposeUiTest {
        var backClicks = 0
        setContent {
            MainScreen(onNavigateBack = { backClicks++ })
        }

        onNodeWithContentDescription(BACK_DESCRIPTION).performClick()

        assertEquals(1, backClicks)
    }

    @Composable
    private fun MainScreen(
        uiState: SettingsUiState = UI_STATE,
        onAction: (Action) -> Unit = {},
        onNavigateBack: () -> Unit = {},
        onNavigateToAbout: () -> Unit = {},
    ) {
        SettingsMainScreen(
            uiState = uiState,
            onAction = onAction,
            onNavigateBack = onNavigateBack,
            onNavigateToAbout = onNavigateToAbout,
        )
    }

    private companion object {
        const val MY_INFO_SUMMARY = "Anna Smith"
        const val FAMILY_NAME_FIRST_LABEL = "Last name"
        const val BACK_DESCRIPTION = "Back"
        const val DISPLAY_SECTION_TITLE = "Display options"

        val UI_STATE = SettingsUiState(
            groups = persistentListOf(
                SettingsGroupUiModel(
                    id = SettingsGroupId.PROFILE,
                    items = persistentListOf(
                        SettingsItemUiModel(
                            id = SettingsItemId.MY_INFO,
                            title = "My info",
                            summary = MY_INFO_SUMMARY,
                        ),
                    ),
                ),
                SettingsGroupUiModel(
                    id = SettingsGroupId.DISPLAY,
                    title = DISPLAY_SECTION_TITLE,
                    items = persistentListOf(
                        SettingsItemUiModel(
                            id = SettingsItemId.SORT_ORDER,
                            title = "Sort by",
                            summary = "First name",
                        ),
                    ),
                ),
                SettingsGroupUiModel(
                    id = SettingsGroupId.ABOUT,
                    items = persistentListOf(
                        SettingsItemUiModel(
                            id = SettingsItemId.ABOUT,
                            title = "About Contacts",
                        ),
                    ),
                ),
            ),
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
        )
    }
}
