package com.android.contacts.ui.settings.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun whenThereIsNoSummary_showsOnlyTitle() = runComposeUiTest {
        setContent {
            SettingsCell(
                title = "Accounts",
                isFirst = true,
                isLast = true,
                onClick = {},
            )
        }

        onNodeWithText("Accounts").assertIsDisplayed()
        onNodeWithText("Accounts").assertHasClickAction()
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
}
