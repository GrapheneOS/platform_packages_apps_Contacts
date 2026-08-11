package com.android.contacts.ui.common.components

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class OverflowMenuTest {

    @Test
    fun untilOpened_showsNoItems() = runComposeUiTest {
        setShareMenuContent()

        onNodeWithText("Share").assertDoesNotExist()
    }

    @Test
    fun whenOpened_showsItsItems() = runComposeUiTest {
        setShareMenuContent()

        openTheMenu()

        onNodeWithText("Share").assertIsDisplayed()
    }

    @Test
    fun whenAnItemIsClicked_reportsItAndCloses() = runComposeUiTest {
        var clicks = 0
        setShareMenuContent(onShareClick = { clicks++ })

        openTheMenu()
        onNodeWithText("Share").performClick()

        assertEquals(1, clicks)
        onNodeWithText("Share").assertDoesNotExist()
    }

    private fun ComposeUiTest.openTheMenu() {
        onNodeWithContentDescription("More options").performClick()
    }

    private fun ComposeUiTest.setShareMenuContent(onShareClick: () -> Unit = {}) {
        setContent {
            OverflowMenu { dismiss ->
                OverflowMenuItem(
                    labelResId = R.string.menu_share,
                    onClick = {
                        onShareClick()
                        dismiss()
                    },
                )
            }
        }
    }
}
