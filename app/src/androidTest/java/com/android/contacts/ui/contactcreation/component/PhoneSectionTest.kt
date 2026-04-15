package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PhoneSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersPhoneField() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).assertIsDisplayed()
    }

    @Test
    fun rendersAddPhoneButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.PHONE_ADD).assertIsDisplayed()
    }

    @Test
    fun typeInPhone_dispatchesUpdatePhone() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).performTextInput("555")
        assertIs<ContactCreationAction.UpdatePhone>(capturedActions.last())
    }

    @Test
    fun tapAddPhone_dispatchesAddPhoneAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.PHONE_ADD).performClick()
        assertEquals(ContactCreationAction.AddPhone, capturedActions.last())
    }

    @Test
    fun multiplePhones_showsDeleteButtons() {
        val phones = listOf(
            PhoneFieldState(id = "1", number = "111"),
            PhoneFieldState(id = "2", number = "222"),
        )
        setContent(phones = phones)
        composeTestRule.onNodeWithTag(TestTags.phoneDelete(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.phoneDelete(1)).assertIsDisplayed()
    }

    @Test
    fun tapDeletePhone_dispatchesRemovePhoneAction() {
        val phones = listOf(
            PhoneFieldState(id = "1", number = "111"),
            PhoneFieldState(id = "2", number = "222"),
        )
        setContent(phones = phones)
        composeTestRule.onNodeWithTag(TestTags.phoneDelete(1)).performClick()
        assertIs<ContactCreationAction.RemovePhone>(capturedActions.last())
    }

    @Test
    fun rendersPhoneTypeSelector() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.phoneType(0)).assertIsDisplayed()
    }

    @Test
    fun tapPhoneType_showsDropdownMenu() {
        val phone = PhoneFieldState(id = "1", number = "555", type = PhoneType.Mobile)
        setContent(phones = listOf(phone))
        val homeLabel = composeTestRule.activity.getString(R.string.field_type_home)
        composeTestRule.onNodeWithTag(TestTags.phoneType(0)).performClick()
        composeTestRule.onNodeWithTag(TestTags.fieldTypeOption(homeLabel)).assertIsDisplayed()
    }

    @Test
    fun selectPhoneType_dispatchesUpdatePhoneType() {
        val phone = PhoneFieldState(id = "1", number = "555", type = PhoneType.Mobile)
        setContent(phones = listOf(phone))
        val homeLabel = composeTestRule.activity.getString(R.string.field_type_home)
        composeTestRule.onNodeWithTag(TestTags.phoneType(0)).performClick()
        composeTestRule.onNodeWithTag(TestTags.fieldTypeOption(homeLabel)).performClick()
        assertIs<ContactCreationAction.UpdatePhoneType>(capturedActions.last())
    }

    private fun setContent(phones: List<PhoneFieldState> = listOf(PhoneFieldState())) {
        composeTestRule.setContent {
            AppTheme {
                LazyColumn {
                    phoneSection(
                        phones = phones,
                        onAction = { capturedActions.add(it) },
                    )
                }
            }
        }
    }
}
