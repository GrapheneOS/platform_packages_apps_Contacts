package com.android.contacts.ui.contactcreation.component

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PhotoSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun noPhoto_showsPlaceholderIcon() {
        setContent(photoUri = null)
        composeTestRule.onNodeWithTag(TestTags.PHOTO_PLACEHOLDER_ICON).assertIsDisplayed()
    }

    @Test
    fun withPhoto_showsAvatar() {
        setContent(photoUri = Uri.parse("content://media/external/images/1234"))
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).assertIsDisplayed()
    }

    @Test
    fun tapAvatar_showsDropdownMenu() {
        setContent(photoUri = null)
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_PICK_GALLERY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_TAKE_CAMERA).assertIsDisplayed()
    }

    @Test
    fun tapAvatar_withPhoto_showsRemoveOption() {
        setContent(photoUri = Uri.parse("content://media/external/images/1234"))
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_REMOVE).assertIsDisplayed()
    }

    @Test
    fun tapAvatar_withoutPhoto_noRemoveOption() {
        setContent(photoUri = null)
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_REMOVE).assertDoesNotExist()
    }

    @Test
    fun tapGallery_dispatchesRequestGalleryEffect() {
        setContent(photoUri = null)
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_PICK_GALLERY).performClick()
        assertEquals(1, capturedActions.size)
        assertIs<ContactCreationAction.RequestGallery>(capturedActions.last())
    }

    @Test
    fun tapCamera_dispatchesRequestCameraEffect() {
        setContent(photoUri = null)
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_TAKE_CAMERA).performClick()
        assertEquals(1, capturedActions.size)
        assertIs<ContactCreationAction.RequestCamera>(capturedActions.last())
    }

    @Test
    fun tapRemove_dispatchesRemovePhoto() {
        setContent(photoUri = Uri.parse("content://media/external/images/1234"))
        composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR).performClick()
        composeTestRule.onNodeWithTag(TestTags.PHOTO_REMOVE).performClick()
        assertEquals(ContactCreationAction.RemovePhoto, capturedActions.last())
    }

    private fun setContent(photoUri: Uri? = null) {
        composeTestRule.setContent {
            AppTheme {
                LazyColumn {
                    photoSection(
                        photoUri = photoUri,
                        onAction = { capturedActions.add(it) },
                    )
                }
            }
        }
    }
}
