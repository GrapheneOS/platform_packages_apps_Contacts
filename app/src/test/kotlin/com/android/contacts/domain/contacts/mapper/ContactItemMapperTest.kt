package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.DirectoryContactColumn
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ContactItemMapperTest {

    private val subject = ContactItemMapperImpl()

    // ContactColumn

    @Test
    fun mapContact_mapsAllFields() {
        val column = ContactColumn(
            id = 1L,
            displayNamePrimary = "John Smith",
            displayNameAlternative = "Smith, John",
            photo = PHOTO,
            sectionLabel = "J",
            isStarred = true,
        )

        val result = subject.map(column, DisplayOrder.GIVEN_NAME_FIRST)

        assertEquals(
            ContactItem(
                key = ContactItemKey.Local(1L),
                name = "John Smith",
                contactPhoto = PHOTO,
                sectionLabel = "J",
                isFavorite = true,
            ),
            result,
        )
    }

    @Test
    fun mapContact_withFamilyNameFirst_usesAlternativeName() {
        val column = ContactColumn(
            id = 1L,
            displayNamePrimary = "John Smith",
            displayNameAlternative = "Smith, John",
        )

        val result = subject.map(column, DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("Smith, John", result.name)
    }

    @Test
    fun mapContact_withFamilyNameFirstAndBlankAlternative_fallsBackToPrimary() {
        val column = ContactColumn(
            id = 1L,
            displayNamePrimary = "John Smith",
            displayNameAlternative = " ",
        )

        val result = subject.map(column, DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("John Smith", result.name)
    }

    @Test
    fun mapContact_withFamilyNameFirstAndNullAlternative_fallsBackToPrimary() {
        val column = ContactColumn(id = 1L, displayNamePrimary = "John Smith")

        val result = subject.map(column, DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("John Smith", result.name)
    }

    @Test
    fun mapContact_withoutNames_returnsEmptyName() {
        val result = subject.map(ContactColumn(id = 1L), DisplayOrder.GIVEN_NAME_FIRST)

        assertEquals("", result.name)
    }

    // ContactDataColumn

    @Test
    fun mapContactData_mapsAllFields() {
        val column = ContactDataColumn(
            id = 2L,
            kind = ContactDataKind.Phone(),
            displayNamePrimary = "Jane Doe",
            displayNameAlternative = "Doe, Jane",
            photo = PHOTO,
            value = "+1 555 0100",
            type = 2,
            label = "Custom",
            sectionLabel = "J",
            isStarred = true,
        )

        val result = subject.map(column, DisplayOrder.GIVEN_NAME_FIRST)

        assertEquals(
            ContactItem(
                key = ContactItemKey.Local(2L),
                name = "Jane Doe",
                contactPhoto = PHOTO,
                field = ContactItem.Field(
                    value = "+1 555 0100",
                    kind = ContactItem.FieldKind.PHONE,
                    type = 2,
                    label = "Custom",
                ),
                sectionLabel = "J",
                isFavorite = true,
            ),
            result,
        )
    }

    @Test
    fun mapContactData_withFamilyNameFirst_usesAlternativeName() {
        val column = ContactDataColumn(
            id = 2L,
            kind = ContactDataKind.Email,
            displayNamePrimary = "Jane Doe",
            displayNameAlternative = "Doe, Jane",
        )

        val result = subject.map(column, DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("Doe, Jane", result.name)
    }

    @Test
    fun mapContactData_mapsKindToFieldKind() {
        val expected = mapOf(
            ContactDataKind.Phone(callableOnly = false) to ContactItem.FieldKind.PHONE,
            ContactDataKind.Phone(callableOnly = true) to ContactItem.FieldKind.PHONE,
            ContactDataKind.Email to ContactItem.FieldKind.EMAIL,
            ContactDataKind.PostalAddress to ContactItem.FieldKind.POSTAL_ADDRESS,
        )

        expected.forEach { (kind, fieldKind) ->
            val column = ContactDataColumn(id = 2L, kind = kind, value = "value")

            val result = subject.map(column, DisplayOrder.GIVEN_NAME_FIRST)

            assertEquals(fieldKind, result.field?.kind)
        }
    }

    @Test
    fun mapContactData_withNullValue_returnsNullField() {
        val column = ContactDataColumn(id = 2L, kind = ContactDataKind.Email, value = null)

        assertNull(subject.map(column, DisplayOrder.GIVEN_NAME_FIRST).field)
    }

    @Test
    fun mapContactData_withBlankValue_returnsNullField() {
        val column = ContactDataColumn(id = 2L, kind = ContactDataKind.Email, value = "  ")

        assertNull(subject.map(column, DisplayOrder.GIVEN_NAME_FIRST).field)
    }

    // DirectoryContactColumn

    @Test
    fun mapDirectoryContact_mapsAllFields() {
        val column = DirectoryContactColumn(
            directoryId = 10L,
            id = 3L,
            lookupKey = "lookup",
            displayNamePrimary = "Alex Brown",
            displayNameAlternative = "Brown, Alex",
            photo = PHOTO,
        )

        val result = subject.map(column, DisplayOrder.GIVEN_NAME_FIRST)

        assertEquals(
            ContactItem(
                key = ContactItemKey.Directory(
                    directoryId = 10L,
                    id = 3L,
                    lookupKey = "lookup",
                ),
                name = "Alex Brown",
                contactPhoto = PHOTO,
            ),
            result,
        )
    }

    @Test
    fun mapDirectoryContact_withFamilyNameFirst_usesAlternativeName() {
        val column = DirectoryContactColumn(
            directoryId = 10L,
            id = 3L,
            lookupKey = "lookup",
            displayNamePrimary = "Alex Brown",
            displayNameAlternative = "Brown, Alex",
        )

        val result = subject.map(column, DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("Brown, Alex", result.name)
    }

    // LegacyContactColumn

    @Test
    fun mapLegacyContact_mapsAllFields() {
        val column = LegacyContactColumn(
            id = 4L,
            kind = LegacyContactsKind.PHONES,
            displayName = "Sam Green",
            value = "+1 555 0199",
            type = 1,
            label = "Home",
            isStarred = true,
        )

        val result = subject.map(column)

        assertEquals(
            ContactItem(
                key = ContactItemKey.Local(4L),
                name = "Sam Green",
                field = ContactItem.Field(
                    value = "+1 555 0199",
                    kind = ContactItem.FieldKind.PHONE,
                    type = 1,
                    label = "Home",
                ),
                isFavorite = true,
            ),
            result,
        )
    }

    @Test
    fun mapLegacyContact_mapsKindToFieldKind() {
        val expected = mapOf(
            LegacyContactsKind.PEOPLE to ContactItem.FieldKind.UNSPECIFIED,
            LegacyContactsKind.PHONES to ContactItem.FieldKind.PHONE,
            LegacyContactsKind.POSTAL_ADDRESSES to ContactItem.FieldKind.POSTAL_ADDRESS,
        )

        expected.forEach { (kind, fieldKind) ->
            val column = LegacyContactColumn(id = 4L, kind = kind, value = "value")

            assertEquals(fieldKind, subject.map(column).field?.kind)
        }
    }

    @Test
    fun mapLegacyContact_withBlankValue_returnsNullField() {
        val column = LegacyContactColumn(id = 4L, kind = LegacyContactsKind.PEOPLE, value = "")

        assertNull(subject.map(column).field)
    }

    companion object {
        private val PHOTO = ContactPhoto.Uri("content://photo/1")
    }
}
