package com.android.contacts.ui.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.settings.screen.model.ABOUT_BUILD_VERSION_TEST_TAG
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_TEST_TAG
import com.android.contacts.ui.settings.screen.model.SETTINGS_ITEM_TEST_TAG_PREFIX
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
internal class SettingsContentTest {

    private fun ComposeUiTest.openAbout() {
        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.ABOUT.name).performClick()
    }

    @Test
    fun startsOnTheMainScreen() = runComposeUiTest {
        setContent {
            Content()
        }

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name)
            .assertIsDisplayed()
        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenAboutIsClicked_showsTheAboutScreen() = runComposeUiTest {
        setContent {
            Content()
        }

        openAbout()

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name)
            .assertDoesNotExist()
    }

    @Test
    fun whenLicensesIsClickedOnAbout_reportsTheAction() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContent {
            Content(onAction = actions::add)
        }
        openAbout()

        onNodeWithTag(ABOUT_LICENSES_TEST_TAG).performClick()

        assertEquals(listOf(Action.LicensesClicked), actions)
    }

    @Test
    fun whenBackIsPressedOnAbout_returnsToTheMainScreen() = runComposeUiTest {
        var backClicks = 0
        setContent {
            Content(onNavigateBack = { backClicks++ })
        }
        openAbout()

        onNodeWithContentDescription(BACK_DESCRIPTION).performClick()

        onNodeWithTag(SETTINGS_ITEM_TEST_TAG_PREFIX + SettingsItemId.MY_INFO.name)
            .assertIsDisplayed()
        assertEquals(0, backClicks)
    }

    @Test
    fun whenBackIsPressedOnTheMainScreen_leavesTheSettings() = runComposeUiTest {
        var backClicks = 0
        setContent {
            Content(onNavigateBack = { backClicks++ })
        }

        onNodeWithContentDescription(BACK_DESCRIPTION).performClick()

        assertEquals(1, backClicks)
    }

    @Composable
    private fun Content(
        onAction: (Action) -> Unit = {},
        onNavigateBack: () -> Unit = {},
    ) {
        SettingsContent(
            uiState = UI_STATE,
            onAction = onAction,
            onNavigateBack = onNavigateBack,
        )
    }

    private companion object {
        const val BACK_DESCRIPTION = "Back"

        val UI_STATE = SettingsUiState(
            groups = persistentListOf(
                SettingsGroupUiModel(
                    id = SettingsGroupId.PROFILE,
                    items = persistentListOf(
                        SettingsItemUiModel(
                            id = SettingsItemId.MY_INFO,
                            title = "My info",
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
            buildVersion = "1.7.40",
        )
    }
}
