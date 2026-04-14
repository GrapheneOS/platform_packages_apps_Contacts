package com.android.contacts.ui.contactcreation.mapper

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RawContactDeltaMapperTest {

    private val mapper = RawContactDeltaMapper()

    @Test
    fun mapsName_toStructuredNameDelta() {
        val state = ContactCreationUiState(
            nameState = NameState(first = "John", last = "Doe"),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredName.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("John", entries[0].getAsString(StructuredName.GIVEN_NAME))
        assertEquals("Doe", entries[0].getAsString(StructuredName.FAMILY_NAME))
    }

    @Test
    fun mapsFullName_withAllFields() {
        val state = ContactCreationUiState(
            nameState = NameState(
                prefix = "Dr",
                first = "John",
                middle = "M",
                last = "Doe",
                suffix = "Jr",
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(StructuredName.CONTENT_ITEM_TYPE)!![0]

        assertEquals("Dr", entry.getAsString(StructuredName.PREFIX))
        assertEquals("John", entry.getAsString(StructuredName.GIVEN_NAME))
        assertEquals("M", entry.getAsString(StructuredName.MIDDLE_NAME))
        assertEquals("Doe", entry.getAsString(StructuredName.FAMILY_NAME))
        assertEquals("Jr", entry.getAsString(StructuredName.SUFFIX))
    }

    @Test
    fun emptyName_notIncluded() {
        val state = ContactCreationUiState(
            nameState = NameState(),
            phoneNumbers = listOf(PhoneFieldState(number = "555")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredName.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun mapsPhone_toPhoneDelta() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(PhoneFieldState(number = "555-1234", type = PhoneType.Mobile)),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("555-1234", entries[0].getAsString(Phone.NUMBER))
        assertEquals(Phone.TYPE_MOBILE, entries[0].getAsInteger(Phone.TYPE))
    }

    @Test
    fun emptyPhone_notIncluded() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(PhoneFieldState(number = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun multiplePhones_producesMultipleEntries() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(
                PhoneFieldState(number = "111"),
                PhoneFieldState(number = "222"),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)

        assertEquals(2, entries!!.size)
    }

    @Test
    fun customPhoneType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(
                PhoneFieldState(number = "555", type = PhoneType.Custom("Satellite")),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Phone.TYPE_CUSTOM, entry.getAsInteger(Phone.TYPE))
        assertEquals("Satellite", entry.getAsString(Phone.LABEL))
    }

    @Test
    fun mapsEmail_toEmailDelta() {
        val state = ContactCreationUiState(
            emails = listOf(EmailFieldState(address = "john@example.com", type = EmailType.Home)),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Email.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("john@example.com", entries[0].getAsString(Email.DATA))
        assertEquals(Email.TYPE_HOME, entries[0].getAsInteger(Email.TYPE))
    }

    @Test
    fun emptyEmail_notIncluded() {
        val state = ContactCreationUiState(
            emails = listOf(EmailFieldState(address = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Email.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customEmailType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            emails = listOf(
                EmailFieldState(address = "a@b.com", type = EmailType.Custom("VIP")),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Email.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Email.TYPE_CUSTOM, entry.getAsInteger(Email.TYPE))
        assertEquals("VIP", entry.getAsString(Email.LABEL))
    }

    @Test
    fun nullAccount_setsLocalAccount() {
        val state = ContactCreationUiState(
            nameState = NameState(first = "Test"),
        )
        val result = mapper.map(state, account = null)

        // Local account has null account name and type
        assertNull(result.state[0].values.getAsString("account_name"))
    }

    @Test
    fun mixedEmptyAndFilledFields_onlyMapsFilledOnes() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(
                PhoneFieldState(number = ""),
                PhoneFieldState(number = "555"),
                PhoneFieldState(number = "  "),
            ),
            emails = listOf(
                EmailFieldState(address = ""),
                EmailFieldState(address = "a@b.com"),
            ),
        )
        val result = mapper.map(state, account = null)

        assertEquals(1, result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)!!.size)
        assertEquals(1, result.state[0].getMimeEntries(Email.CONTENT_ITEM_TYPE)!!.size)
    }
}
