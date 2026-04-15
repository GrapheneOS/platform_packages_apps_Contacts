package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FieldTypeSelectorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var selectedType: String? = null
    private val types = listOf("Mobile", "Home", "Work", "Other")

    @Before
    fun setup() {
        selectedType = null
    }

    @Test
    fun showsCurrentTypeLabel() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).assertIsDisplayed()
    }

    @Test
    fun tapOpensDropdown() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).performClick()
        // After click, dropdown items should appear — "Home" is one of them
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun selectType_dispatchesCallback() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).performClick()
        composeTestRule.onNodeWithText("Work").performClick()
        assertEquals("Work", selectedType)
    }

    @Test
    fun menuItemsMatchTypeList() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).performClick()
        // All types should appear in the dropdown
        types.forEach { type ->
            composeTestRule.onNodeWithText(type).assertIsDisplayed()
        }
    }

    @Test
    fun chipHasTestTag() {
        setContent(currentType = "Home")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).assertExists()
    }

    @Test
    fun noSelectionBeforeTap() {
        setContent(currentType = "Mobile")
        assertNull(selectedType)
    }

    private fun setContent(currentType: String) {
        composeTestRule.setContent {
            AppTheme {
                FieldTypeSelector(
                    currentType = currentType,
                    types = types,
                    typeLabel = { it },
                    onTypeSelected = { selectedType = it },
                    modifier = Modifier.testTag(SELECTOR_TAG),
                )
            }
        }
    }

    companion object {
        private const val SELECTOR_TAG = "test_field_type_selector"
    }
}
