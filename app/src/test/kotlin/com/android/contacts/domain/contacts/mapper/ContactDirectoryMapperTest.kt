package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.domain.contacts.model.ContactDirectory
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ContactDirectoryMapperTest {

    private val subject = ContactDirectoryMapperImpl()

    @Test
    fun map_mapsAllFields() {
        val column = DirectoryColumn(
            id = 5L,
            displayName = "Work Directory",
            packageName = "com.example.directory",
            typeResourceId = 42,
        )

        val result = subject.map(column)

        assertEquals(
            ContactDirectory(
                id = 5L,
                displayName = "Work Directory",
                packageName = "com.example.directory",
                typeResourceId = 42,
            ),
            result,
        )
    }

    @Test
    fun map_whenOptionalFieldsAreNull_keepsThemNull() {
        val result = subject.map(DirectoryColumn(id = 5L))

        assertEquals(ContactDirectory(id = 5L), result)
    }
}
