package com.android.contacts.list

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactListFilterTest {
    @Test
    fun whenRestoringFromPreferences_overrideCustomFilterToAll() {
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true) {
            every {
                getInt(ContactListFilter.KEY_FILTER_TYPE, any())
            } returns ContactListFilter.FILTER_TYPE_CUSTOM
        }
        val filter = ContactListFilter.restoreDefaultPreferences(sharedPreferences)

        assertEquals(
            "Filter should have been overriden to FILTER_TYPE_ALL_ACCOUNTS",
            ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS,
            filter.filterType,
        )
    }

    @Test
    fun whenRestoringFromPreferences_keepStarredFilter() {
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true) {
            every {
                getInt(ContactListFilter.KEY_FILTER_TYPE, any())
            } returns ContactListFilter.FILTER_TYPE_STARRED
        }
        val filter = ContactListFilter.restoreDefaultPreferences(sharedPreferences)

        assertEquals(
            "Filter should have been kept to FILTER_TYPE_STARRED",
            ContactListFilter.FILTER_TYPE_STARRED,
            filter.filterType,
        )
    }
}
