package com.android.contacts.ui.contactcreation.delegate

import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("LargeClass")
class ContactFieldsDelegateTest {

    private lateinit var delegate: ContactFieldsDelegate

    @Before
    fun setup() {
        delegate = ContactFieldsDelegate()
    }

    // --- Phone ---

    @Test
    fun initialState_hasOneEmptyPhone() {
        val phones = delegate.getPhones()
        assertEquals(1, phones.size)
        assertTrue(phones[0].number.isEmpty())
    }

    @Test
    fun addPhone_addsEmptyRow() {
        val phones = delegate.addPhone()
        assertEquals(2, phones.size)
        assertTrue(phones[1].number.isEmpty())
    }

    @Test
    fun removePhone_removesById() {
        delegate.addPhone()
        val phones = delegate.getPhones()
        assertEquals(2, phones.size)
        val idToRemove = phones[0].id

        val result = delegate.removePhone(idToRemove)
        assertEquals(1, result.size)
        assertTrue(result.none { it.id == idToRemove })
    }

    @Test
    fun updatePhone_updatesValueById() {
        val id = delegate.getPhones()[0].id
        val result = delegate.updatePhone(id, "555-1234")
        assertEquals("555-1234", result[0].number)
    }

    @Test
    fun updatePhone_nonExistentId_noChange() {
        val result = delegate.updatePhone("nonexistent", "555")
        assertEquals(1, result.size)
        assertTrue(result[0].number.isEmpty())
    }

    @Test
    fun updatePhoneType_changesTypeInState() {
        val id = delegate.getPhones()[0].id
        val result = delegate.updatePhoneType(id, PhoneType.Work)
        assertEquals(PhoneType.Work, result[0].type)
    }

    // --- Email ---

    @Test
    fun initialState_hasOneEmptyEmail() {
        val emails = delegate.getEmails()
        assertEquals(1, emails.size)
        assertTrue(emails[0].address.isEmpty())
    }

    @Test
    fun addEmail_addsEmptyRow() {
        val emails = delegate.addEmail()
        assertEquals(2, emails.size)
    }

    @Test
    fun removeEmail_removesById() {
        delegate.addEmail()
        val id = delegate.getEmails()[0].id
        val result = delegate.removeEmail(id)
        assertEquals(1, result.size)
        assertTrue(result.none { it.id == id })
    }

    @Test
    fun updateEmail_updatesValueById() {
        val id = delegate.getEmails()[0].id
        val result = delegate.updateEmail(id, "a@b.com")
        assertEquals("a@b.com", result[0].address)
    }

    // --- Address ---

    @Test
    fun initialState_hasNoAddresses() {
        assertTrue(delegate.getAddresses().isEmpty())
    }

    @Test
    fun addAddress_addsEmptyRow() {
        val addresses = delegate.addAddress()
        assertEquals(1, addresses.size)
        assertTrue(addresses[0].street.isEmpty())
    }

    @Test
    fun removeAddress_removesById() {
        delegate.addAddress()
        val id = delegate.getAddresses()[0].id
        val result = delegate.removeAddress(id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun updateAddressStreet_updatesValue() {
        delegate.addAddress()
        val id = delegate.getAddresses()[0].id
        val result = delegate.updateAddressStreet(id, "123 Main St")
        assertEquals("123 Main St", result[0].street)
    }

    @Test
    fun updateAddressCity_updatesValue() {
        delegate.addAddress()
        val id = delegate.getAddresses()[0].id
        val result = delegate.updateAddressCity(id, "Chicago")
        assertEquals("Chicago", result[0].city)
    }

    @Test
    fun updateAddressType_updatesValue() {
        delegate.addAddress()
        val id = delegate.getAddresses()[0].id
        val result = delegate.updateAddressType(id, AddressType.Work)
        assertEquals(AddressType.Work, result[0].type)
    }

    @Test
    fun restoreAddresses_replacesInternalState() {
        val restored = listOf(AddressFieldState(street = "Restored St"))
        delegate.restoreAddresses(restored)
        assertEquals("Restored St", delegate.getAddresses()[0].street)
    }

    // --- Event ---

    @Test
    fun initialState_hasNoEvents() {
        assertTrue(delegate.getEvents().isEmpty())
    }

    @Test
    fun addEvent_addsEmptyRow() {
        val events = delegate.addEvent()
        assertEquals(1, events.size)
        assertTrue(events[0].startDate.isEmpty())
    }

    @Test
    fun removeEvent_removesById() {
        delegate.addEvent()
        val id = delegate.getEvents()[0].id
        val result = delegate.removeEvent(id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun updateEvent_updatesValue() {
        delegate.addEvent()
        val id = delegate.getEvents()[0].id
        val result = delegate.updateEvent(id, "1990-01-15")
        assertEquals("1990-01-15", result[0].startDate)
    }

    @Test
    fun updateEventType_updatesValue() {
        delegate.addEvent()
        val id = delegate.getEvents()[0].id
        val result = delegate.updateEventType(id, EventType.Anniversary)
        assertEquals(EventType.Anniversary, result[0].type)
    }

    // --- Relation ---

    @Test
    fun initialState_hasNoRelations() {
        assertTrue(delegate.getRelations().isEmpty())
    }

    @Test
    fun addRelation_addsEmptyRow() {
        val relations = delegate.addRelation()
        assertEquals(1, relations.size)
        assertTrue(relations[0].name.isEmpty())
    }

    @Test
    fun removeRelation_removesById() {
        delegate.addRelation()
        val id = delegate.getRelations()[0].id
        val result = delegate.removeRelation(id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun updateRelation_updatesValue() {
        delegate.addRelation()
        val id = delegate.getRelations()[0].id
        val result = delegate.updateRelation(id, "Jane")
        assertEquals("Jane", result[0].name)
    }

    @Test
    fun updateRelationType_updatesValue() {
        delegate.addRelation()
        val id = delegate.getRelations()[0].id
        val result = delegate.updateRelationType(id, RelationType.Friend)
        assertEquals(RelationType.Friend, result[0].type)
    }

    // --- IM ---

    @Test
    fun initialState_hasNoImAccounts() {
        assertTrue(delegate.getImAccounts().isEmpty())
    }

    @Test
    fun addIm_addsEmptyRow() {
        val ims = delegate.addIm()
        assertEquals(1, ims.size)
        assertTrue(ims[0].data.isEmpty())
    }

    @Test
    fun removeIm_removesById() {
        delegate.addIm()
        val id = delegate.getImAccounts()[0].id
        val result = delegate.removeIm(id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun updateIm_updatesValue() {
        delegate.addIm()
        val id = delegate.getImAccounts()[0].id
        val result = delegate.updateIm(id, "user@jabber.org")
        assertEquals("user@jabber.org", result[0].data)
    }

    @Test
    fun updateImProtocol_updatesValue() {
        delegate.addIm()
        val id = delegate.getImAccounts()[0].id
        val result = delegate.updateImProtocol(id, ImProtocol.Skype)
        assertEquals(ImProtocol.Skype, result[0].protocol)
    }

    // --- Website ---

    @Test
    fun initialState_hasNoWebsites() {
        assertTrue(delegate.getWebsites().isEmpty())
    }

    @Test
    fun addWebsite_addsEmptyRow() {
        val websites = delegate.addWebsite()
        assertEquals(1, websites.size)
        assertTrue(websites[0].url.isEmpty())
    }

    @Test
    fun removeWebsite_removesById() {
        delegate.addWebsite()
        val id = delegate.getWebsites()[0].id
        val result = delegate.removeWebsite(id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun updateWebsite_updatesValue() {
        delegate.addWebsite()
        val id = delegate.getWebsites()[0].id
        val result = delegate.updateWebsite(id, "https://example.com")
        assertEquals("https://example.com", result[0].url)
    }

    @Test
    fun updateWebsiteType_updatesValue() {
        delegate.addWebsite()
        val id = delegate.getWebsites()[0].id
        val result = delegate.updateWebsiteType(id, WebsiteType.Blog)
        assertEquals(WebsiteType.Blog, result[0].type)
    }

    // --- Group ---

    @Test
    fun initialState_hasNoGroups() {
        assertTrue(delegate.getGroups().isEmpty())
    }

    @Test
    fun toggleGroup_addsGroup() {
        val groups = delegate.toggleGroup(42L, "Friends")
        assertEquals(1, groups.size)
        assertEquals(42L, groups[0].groupId)
        assertEquals("Friends", groups[0].title)
    }

    @Test
    fun toggleGroup_removesIfAlreadySelected() {
        delegate.toggleGroup(42L, "Friends")
        val groups = delegate.toggleGroup(42L, "Friends")
        assertTrue(groups.isEmpty())
    }

    @Test
    fun clearGroups_removesAll() {
        delegate.toggleGroup(1L, "A")
        delegate.toggleGroup(2L, "B")
        val groups = delegate.clearGroups()
        assertTrue(groups.isEmpty())
    }

    // --- Restore ---

    @Test
    fun restorePhones_replacesInternalState() {
        val id = delegate.getPhones()[0].id
        delegate.updatePhone(id, "old")

        val newPhones = listOf(
            com.android.contacts.ui.contactcreation.model.PhoneFieldState(number = "restored"),
        )
        delegate.restorePhones(newPhones)

        assertEquals("restored", delegate.getPhones()[0].number)
    }

    @Test
    fun restoreEvents_replacesInternalState() {
        val restored = listOf(EventFieldState(startDate = "2020-01-01"))
        delegate.restoreEvents(restored)
        assertEquals("2020-01-01", delegate.getEvents()[0].startDate)
    }

    @Test
    fun restoreRelations_replacesInternalState() {
        val restored = listOf(RelationFieldState(name = "Bob"))
        delegate.restoreRelations(restored)
        assertEquals("Bob", delegate.getRelations()[0].name)
    }

    @Test
    fun restoreImAccounts_replacesInternalState() {
        val restored = listOf(ImFieldState(data = "user@im"))
        delegate.restoreImAccounts(restored)
        assertEquals("user@im", delegate.getImAccounts()[0].data)
    }

    @Test
    fun restoreWebsites_replacesInternalState() {
        val restored = listOf(WebsiteFieldState(url = "https://restored.com"))
        delegate.restoreWebsites(restored)
        assertEquals("https://restored.com", delegate.getWebsites()[0].url)
    }
}
