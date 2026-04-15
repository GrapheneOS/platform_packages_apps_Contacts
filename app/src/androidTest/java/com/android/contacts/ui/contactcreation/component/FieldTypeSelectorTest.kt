package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.core.AppTheme
import org.junit.Assert.assertEquals
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
        composeTestRule.onNodeWithTag(TestTags.fieldTypeOption("Home")).assertIsDisplayed()
    }

    @Test
    fun selectType_dispatchesCallback() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).performClick()
        composeTestRule.onNodeWithTag(TestTags.fieldTypeOption("Work")).performClick()
        assertEquals("Work", selectedType)
    }

    @Test
    fun menuItemsMatchTypeList() {
        setContent(currentType = "Mobile")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).performClick()
        types.forEach { type ->
            composeTestRule.onNodeWithTag(TestTags.fieldTypeOption(type)).assertIsDisplayed()
        }
    }

    @Test
    fun chipHasTestTag() {
        setContent(currentType = "Home")
        composeTestRule.onNodeWithTag(SELECTOR_TAG).assertExists()
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
