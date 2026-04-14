package com.android.contacts.ui.contactcreation.delegate

import com.android.contacts.ui.contactcreation.component.PhoneType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
}
