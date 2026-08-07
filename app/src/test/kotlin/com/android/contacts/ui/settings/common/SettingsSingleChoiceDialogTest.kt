package com.android.contacts.ui.settings.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.settings.screen.model.SettingsChoice
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SettingsSingleChoiceDialogTest {

    @Test
    fun showsTitleAndEveryOption() = runComposeUiTest {
        setContent {
            SortOrderDialog()
        }

        onNodeWithText(TITLE).assertIsDisplayed()
        onNodeWithText(GIVEN_NAME).assertIsDisplayed()
        onNodeWithText(FAMILY_NAME).assertIsDisplayed()
    }

    @Test
    fun marksTheSelectedOption() = runComposeUiTest {
        setContent {
            SortOrderDialog(selected = FAMILY_NAME_VALUE)
        }

        onNodeWithText(FAMILY_NAME).assertIsSelected()
        onNodeWithText(GIVEN_NAME).assertIsNotSelected()
    }

    @Test
    fun whenOptionIsClicked_reportsItsValue() = runComposeUiTest {
        var selectedValue: String? = null
        setContent {
            SortOrderDialog(onSelect = { selectedValue = it })
        }

        onNodeWithText(FAMILY_NAME).performClick()

        assertEquals(FAMILY_NAME_VALUE, selectedValue)
    }

    @Test
    fun hasNoConfirmationButtons() = runComposeUiTest {
        setContent {
            SortOrderDialog()
        }

        onNodeWithText("OK").assertDoesNotExist()
        onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Composable
    private fun SortOrderDialog(
        selected: String = GIVEN_NAME_VALUE,
        onSelect: (String) -> Unit = {},
    ) {
        SettingsSingleChoiceDialog(
            title = TITLE,
            options = persistentListOf(
                SettingsChoice(value = GIVEN_NAME_VALUE, label = GIVEN_NAME),
                SettingsChoice(value = FAMILY_NAME_VALUE, label = FAMILY_NAME),
            ),
            selected = selected,
            onSelect = onSelect,
            onDismissRequest = {},
        )
    }

    private companion object {
        const val TITLE = "Sort by"
        const val GIVEN_NAME = "First name"
        const val FAMILY_NAME = "Last name"
        const val GIVEN_NAME_VALUE = "given"
        const val FAMILY_NAME_VALUE = "family"
    }
}
