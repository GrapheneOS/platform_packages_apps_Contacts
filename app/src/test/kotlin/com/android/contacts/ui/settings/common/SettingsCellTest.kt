package com.android.contacts.ui.settings.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SettingsCellTest {

    @Test
    fun showsTitleAndSummary() = runComposeUiTest {
        setContent {
            SettingsCell(
                title = "My info",
                summary = "Anna Smith",
                isFirst = true,
                isLast = true,
                onClick = {},
            )
        }

        onNodeWithText("My info").assertIsDisplayed()
        onNodeWithText("Anna Smith").assertIsDisplayed()
    }

    @Test
    fun whenClicked_reportsTheClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            SettingsCell(
                title = "Import",
                isFirst = true,
                isLast = true,
                onClick = { clicks++ },
            )
        }

        onNodeWithText("Import").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun whenLongPressed_reportsTheLongClick() = runComposeUiTest {
        var longClicks = 0
        setContent {
            SettingsCell(
                title = "Build version",
                summary = "1.7.40",
                isFirst = true,
                isLast = true,
                onLongClick = { longClicks++ },
                onLongClickLabel = "Copy to clipboard",
            )
        }

        onNodeWithText("Build version").performTouchInput { longClick() }

        assertEquals(1, longClicks)
    }

    @Test
    fun whenOnlyLongClickIsSet_theCellIsNotClickable() = runComposeUiTest {
        setContent {
            SettingsCell(
                title = "Build version",
                isFirst = true,
                isLast = true,
                onLongClick = {},
            )
        }

        onNodeWithText("Build version").assertHasNoClickAction()
    }
}
