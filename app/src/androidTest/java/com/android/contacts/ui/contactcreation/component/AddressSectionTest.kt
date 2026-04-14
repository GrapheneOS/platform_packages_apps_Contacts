package com.android.contacts.ui.contactcreation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.core.AppTheme
import kotlin.test.assertIs
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddressSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    @Test
    fun rendersAddAddressButton() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ADDRESS_ADD).assertIsDisplayed()
    }

    @Test
    fun tapAddAddress_dispatchesAddAddressAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.ADDRESS_ADD).performClick()
        assertEquals(ContactCreationAction.AddAddress, capturedActions.last())
    }

    @Test
    fun rendersAllAddressSubFields() {
        val addresses = listOf(AddressFieldState(id = "1"))
        setContent(addresses = addresses)
        composeTestRule.onNodeWithTag(TestTags.addressStreet(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.addressCity(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.addressRegion(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.addressPostcode(0)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.addressCountry(0)).assertIsDisplayed()
    }

    @Test
    fun typeInStreet_dispatchesUpdateAddressStreet() {
        val addresses = listOf(AddressFieldState(id = "1"))
        setContent(addresses = addresses)
        composeTestRule.onNodeWithTag(TestTags.addressStreet(0)).performTextInput("123 Main")
        assertIs<ContactCreationAction.UpdateAddressStreet>(capturedActions.last())
    }

    @Test
    fun typeInCity_dispatchesUpdateAddressCity() {
        val addresses = listOf(AddressFieldState(id = "1"))
        setContent(addresses = addresses)
        composeTestRule.onNodeWithTag(TestTags.addressCity(0)).performTextInput("Chicago")
        assertIs<ContactCreationAction.UpdateAddressCity>(capturedActions.last())
    }

    @Test
    fun tapDeleteAddress_dispatchesRemoveAddressAction() {
        val addresses = listOf(AddressFieldState(id = "1"))
        setContent(addresses = addresses)
        composeTestRule.onNodeWithTag(TestTags.addressDelete(0)).performClick()
        assertIs<ContactCreationAction.RemoveAddress>(capturedActions.last())
    }

    private fun setContent(addresses: List<AddressFieldState> = emptyList()) {
        composeTestRule.setContent {
            AppTheme {
                LazyColumn {
                    addressSection(
                        addresses = addresses,
                        onAction = { capturedActions.add(it) },
                    )
                }
            }
        }
    }
}
