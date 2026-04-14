package com.android.contacts.ui.contactcreation.mapper

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class RawContactDeltaMapperTest {

    private val mapper = RawContactDeltaMapper()

    // --- Name ---

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

    // --- Phone ---

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

    // --- Email ---

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

    // --- Address ---

    @Test
    fun mapsAddress_toStructuredPostalDelta() {
        val state = ContactCreationUiState(
            addresses = listOf(
                AddressFieldState(
                    street = "123 Main St",
                    city = "Springfield",
                    region = "IL",
                    postcode = "62701",
                    country = "US",
                    type = AddressType.Home,
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("123 Main St", entries[0].getAsString(StructuredPostal.STREET))
        assertEquals("Springfield", entries[0].getAsString(StructuredPostal.CITY))
        assertEquals("IL", entries[0].getAsString(StructuredPostal.REGION))
        assertEquals("62701", entries[0].getAsString(StructuredPostal.POSTCODE))
        assertEquals("US", entries[0].getAsString(StructuredPostal.COUNTRY))
        assertEquals(StructuredPostal.TYPE_HOME, entries[0].getAsInteger(StructuredPostal.TYPE))
    }

    @Test
    fun emptyAddress_notIncluded() {
        val state = ContactCreationUiState(
            addresses = listOf(AddressFieldState()),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun addressWithOnlyCityFilled_isIncluded() {
        val state = ContactCreationUiState(
            addresses = listOf(AddressFieldState(city = "Chicago")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Chicago", entries[0].getAsString(StructuredPostal.CITY))
    }

    @Test
    fun customAddressType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            addresses = listOf(
                AddressFieldState(
                    street = "1 Elm",
                    type = AddressType.Custom("Vacation"),
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)!![0]

        assertEquals(StructuredPostal.TYPE_CUSTOM, entry.getAsInteger(StructuredPostal.TYPE))
        assertEquals("Vacation", entry.getAsString(StructuredPostal.LABEL))
    }

    // --- Organization ---

    @Test
    fun mapsOrganization_toOrgDelta() {
        val state = ContactCreationUiState(
            organization = OrganizationFieldState(company = "Acme", title = "Engineer"),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Organization.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Acme", entries[0].getAsString(Organization.COMPANY))
        assertEquals("Engineer", entries[0].getAsString(Organization.TITLE))
    }

    @Test
    fun emptyOrganization_notIncluded() {
        val state = ContactCreationUiState(
            organization = OrganizationFieldState(),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Organization.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun orgWithOnlyCompany_isIncluded() {
        val state = ContactCreationUiState(
            organization = OrganizationFieldState(company = "Acme"),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Organization.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Acme", entries[0].getAsString(Organization.COMPANY))
    }

    // --- Note ---

    @Test
    fun mapsNote_toNoteDelta() {
        val state = ContactCreationUiState(note = "Important person")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Note.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Important person", entries[0].getAsString(Note.NOTE))
    }

    @Test
    fun emptyNote_notIncluded() {
        val state = ContactCreationUiState(note = "")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Note.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    // --- Website ---

    @Test
    fun mapsWebsite_toWebsiteDelta() {
        val state = ContactCreationUiState(
            websites = listOf(
                WebsiteFieldState(url = "https://example.com", type = WebsiteType.Homepage),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Website.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("https://example.com", entries[0].getAsString(Website.URL))
        assertEquals(Website.TYPE_HOMEPAGE, entries[0].getAsInteger(Website.TYPE))
    }

    @Test
    fun emptyWebsite_notIncluded() {
        val state = ContactCreationUiState(
            websites = listOf(WebsiteFieldState(url = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Website.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customWebsiteType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            websites = listOf(
                WebsiteFieldState(
                    url = "https://blog.example.com",
                    type = WebsiteType.Custom("Portfolio"),
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Website.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Website.TYPE_CUSTOM, entry.getAsInteger(Website.TYPE))
        assertEquals("Portfolio", entry.getAsString(Website.LABEL))
    }

    // --- Event ---

    @Test
    fun mapsEvent_toEventDelta() {
        val state = ContactCreationUiState(
            events = listOf(
                EventFieldState(startDate = "1990-01-15", type = EventType.Birthday),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Event.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("1990-01-15", entries[0].getAsString(Event.START_DATE))
        assertEquals(Event.TYPE_BIRTHDAY, entries[0].getAsInteger(Event.TYPE))
    }

    @Test
    fun emptyEvent_notIncluded() {
        val state = ContactCreationUiState(
            events = listOf(EventFieldState(startDate = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Event.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customEventType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            events = listOf(
                EventFieldState(
                    startDate = "2020-06-01",
                    type = EventType.Custom("First met"),
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Event.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Event.TYPE_CUSTOM, entry.getAsInteger(Event.TYPE))
        assertEquals("First met", entry.getAsString(Event.LABEL))
    }

    // --- Relation ---

    @Test
    fun mapsRelation_toRelationDelta() {
        val state = ContactCreationUiState(
            relations = listOf(
                RelationFieldState(name = "Jane Doe", type = RelationType.Spouse),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Relation.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Jane Doe", entries[0].getAsString(Relation.NAME))
        assertEquals(Relation.TYPE_SPOUSE, entries[0].getAsInteger(Relation.TYPE))
    }

    @Test
    fun emptyRelation_notIncluded() {
        val state = ContactCreationUiState(
            relations = listOf(RelationFieldState(name = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Relation.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customRelationType_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            relations = listOf(
                RelationFieldState(
                    name = "Bob",
                    type = RelationType.Custom("Mentor"),
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Relation.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Relation.TYPE_CUSTOM, entry.getAsInteger(Relation.TYPE))
        assertEquals("Mentor", entry.getAsString(Relation.LABEL))
    }

    // --- IM (PROTOCOL + CUSTOM_PROTOCOL, not TYPE + LABEL) ---

    @Test
    fun mapsIm_toImDelta_withProtocol() {
        val state = ContactCreationUiState(
            imAccounts = listOf(
                ImFieldState(data = "user@jabber.org", protocol = ImProtocol.Jabber),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Im.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("user@jabber.org", entries[0].getAsString(Im.DATA))
        assertEquals(Im.PROTOCOL_JABBER, entries[0].getAsInteger(Im.PROTOCOL))
    }

    @Test
    fun emptyIm_notIncluded() {
        val state = ContactCreationUiState(
            imAccounts = listOf(ImFieldState(data = "")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Im.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customImProtocol_setsProtocolAndCustomProtocol() {
        val state = ContactCreationUiState(
            imAccounts = listOf(
                ImFieldState(
                    data = "user123",
                    protocol = ImProtocol.Custom("Matrix"),
                ),
            ),
        )
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(Im.CONTENT_ITEM_TYPE)!![0]

        assertEquals(Im.PROTOCOL_CUSTOM, entry.getAsInteger(Im.PROTOCOL))
        assertEquals("Matrix", entry.getAsString(Im.CUSTOM_PROTOCOL))
    }

    // --- Nickname ---

    @Test
    fun mapsNickname_toNicknameDelta() {
        val state = ContactCreationUiState(nickname = "Johnny")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Nickname.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals("Johnny", entries[0].getAsString(Nickname.NAME))
    }

    @Test
    fun emptyNickname_notIncluded() {
        val state = ContactCreationUiState(nickname = "")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(Nickname.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    // --- SIP ---

    @Test
    fun mapsSipAddress_toSipDelta() {
        val state = ContactCreationUiState(sipAddress = "sip:user@voip.example.com")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(SipAddress.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals(
            "sip:user@voip.example.com",
            entries[0].getAsString(SipAddress.SIP_ADDRESS),
        )
    }

    @Test
    fun emptySipAddress_notIncluded() {
        val state = ContactCreationUiState(sipAddress = "")
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(SipAddress.CONTENT_ITEM_TYPE)

        assertTrue(entries.isNullOrEmpty())
    }

    // --- Group Membership ---

    @Test
    fun mapsGroup_toGroupMembershipDelta() {
        val state = ContactCreationUiState(
            groups = listOf(GroupFieldState(groupId = 42L, title = "Friends")),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE)

        assertNotNull(entries)
        assertEquals(1, entries!!.size)
        assertEquals(42L, entries[0].getAsLong(GroupMembership.GROUP_ROW_ID))
    }

    @Test
    fun multipleGroups_producesMultipleEntries() {
        val state = ContactCreationUiState(
            groups = listOf(
                GroupFieldState(groupId = 1L, title = "Friends"),
                GroupFieldState(groupId = 2L, title = "Family"),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE)

        assertEquals(2, entries!!.size)
    }

    // --- Account ---

    @Test
    fun nullAccount_setsLocalAccount() {
        val state = ContactCreationUiState(
            nameState = NameState(first = "Test"),
        )
        val result = mapper.map(state, account = null)

        assertNull(result.state[0].values.getAsString("account_name"))
    }

    // --- Mixed fields ---

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

    @Test
    fun multipleAddresses_producesMultipleEntries() {
        val state = ContactCreationUiState(
            addresses = listOf(
                AddressFieldState(street = "1 First St"),
                AddressFieldState(city = "Second City"),
            ),
        )
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE)

        assertEquals(2, entries!!.size)
    }
}
