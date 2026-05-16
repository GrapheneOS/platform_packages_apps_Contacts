package com.android.contacts.editor

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.android.contacts.di.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
internal class ContactEditorTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Test
    fun defaultPhotoPlaceHolderIsShown() {
        ContactEditorRobot(composeTestRule)
            .photoPlaceholderIsShown()
    }

    @Test
    fun clickAddPhotoOpensChooserDialog() {
        ContactEditorRobot(composeTestRule)
            .clickAddPhoto()
            .choosePhotoSourceDialogIsShown()
    }

    @Test
    fun clickPlaceholderOpensChooserDialog() {
        ContactEditorRobot(composeTestRule)
            .clickPhotoPlaceholder()
            .choosePhotoSourceDialogIsShown()
    }
}
