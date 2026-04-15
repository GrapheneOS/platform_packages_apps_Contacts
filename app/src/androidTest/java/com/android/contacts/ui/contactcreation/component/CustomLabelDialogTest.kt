package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CustomLabelDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var confirmedLabel: String? = null
    private var dismissed = false

    @Before
    fun setup() {
        confirmedLabel = null
        dismissed = false
    }

    @Test
    fun showsInputField() {
        setContent()
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_INPUT_TAG).assertIsDisplayed()
    }

    @Test
    fun confirmWithLabel_dispatchesLabel() {
        setContent()
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_INPUT_TAG).performTextInput("Work cell")
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_OK_TAG).performClick()
        assertEquals("Work cell", confirmedLabel)
    }

    @Test
    fun cancelDismisses() {
        setContent()
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_CANCEL_TAG).performClick()
        assertTrue(dismissed)
    }

    @Test
    fun emptyLabel_disablesConfirm() {
        setContent()
        // Don't type anything — confirm should be disabled
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_OK_TAG).assertIsNotEnabled()
    }

    @Test
    fun nonEmptyLabel_enablesConfirm() {
        setContent()
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_INPUT_TAG).performTextInput("Label")
        composeTestRule.onNodeWithTag(CUSTOM_LABEL_OK_TAG).assertIsEnabled()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppTheme {
                CustomLabelDialog(
                    onConfirm = { confirmedLabel = it },
                    onDismiss = { dismissed = true },
                )
            }
        }
    }
}
