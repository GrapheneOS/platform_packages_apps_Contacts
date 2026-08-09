package com.android.contacts.data.contactdetails.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class ContactDisplayNameSourceTest {

    @Test
    fun entries_areOrderedByPlatformPrecedence() {
        val expectedOrder = listOf(
            ContactDisplayNameSource.UNDEFINED,
            ContactDisplayNameSource.EMAIL,
            ContactDisplayNameSource.PHONE,
            ContactDisplayNameSource.ORGANIZATION,
            ContactDisplayNameSource.NICKNAME,
            ContactDisplayNameSource.STRUCTURED_PHONETIC_NAME,
            ContactDisplayNameSource.STRUCTURED_NAME,
        )

        assertEquals(expectedOrder, ContactDisplayNameSource.entries.toList())
    }
}
