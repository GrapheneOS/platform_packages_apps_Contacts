package com.android.contacts.editor

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.contacts.R
import com.android.contacts.di.HiltTestActivity
import com.android.contacts.editornew.ContactEditor
import com.android.contacts.ui.core.AppTheme

internal class ContactEditorRobot(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<HiltTestActivity>, HiltTestActivity>,
) {
    init {
        composeTestRule.setContent {
            AppTheme {
                ContactEditor(
                    onNavigateBack = {},
                )
            }
        }
    }

    fun photoPlaceholderIsShown(): ContactEditorRobot = also {
        assertIsDisplayed(testTag = "contact_editor_photo_placeholder")
    }

    fun choosePhotoSourceDialogIsShown(): ContactEditorRobot = also {
        assertIsDisplayed(testTag = "contact_editor_photo_source_chooser_dialog_content")
    }

    fun clickPhotoPlaceholder(): ContactEditorRobot = also {
        performClick(testTag = "contact_editor_photo_placeholder")
    }

    fun clickAddPhoto(): ContactEditorRobot = also {
        performClick(resId = R.string.contact_editor_photo_add)
    }

    private fun assertIsDisplayed(testTag: String) {
        onNodeWithTag(testTag = testTag)
            .assertIsDisplayed()
    }

    private fun performClick(testTag: String) {
        onNodeWithTag(testTag = testTag)
            .performClick()
    }

    private fun performClick(@StringRes resId: Int) {
        composeTestRule
            .onNodeWithText(resId = resId)
            .performClick()
    }

    private fun onNodeWithTag(testTag: String): SemanticsNodeInteraction {
        return composeTestRule
            .onNodeWithTag(testTag = testTag)
    }

    private fun SemanticsNodeInteractionsProvider.onNodeWithText(
        @StringRes resId: Int,
    ): SemanticsNodeInteraction = onNodeWithText(
        text = composeTestRule.activity.getString(resId),
    )
}
