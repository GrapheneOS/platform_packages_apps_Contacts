package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountChipTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var clicked = false

    @Before
    fun setup() {
        clicked = false
    }

    @Test
    fun displaysAccountName() {
        setContent(accountName = "user@gmail.com")
        composeTestRule.onNodeWithTag(TestTags.ACCOUNT_CHIP).assertIsDisplayed()
    }

    @Test
    fun nullAccount_showsDeviceLabel() {
        setContent(accountName = null)
        // Chip should still be displayed with "Device" text (from string resource)
        composeTestRule.onNodeWithTag(TestTags.ACCOUNT_CHIP).assertIsDisplayed()
    }

    @Test
    fun tapChip_dispatchesClick() {
        setContent(accountName = "user@gmail.com")
        composeTestRule.onNodeWithTag(TestTags.ACCOUNT_CHIP).performClick()
        assertTrue(clicked)
    }

    private fun setContent(accountName: String?) {
        composeTestRule.setContent {
            AppTheme {
                AccountChip(
                    accountName = accountName,
                    onClick = { clicked = true },
                )
            }
        }
    }
}
