@file:Suppress("DEPRECATION")

package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.net.Uri
import android.provider.Contacts.ContactMethods
import android.provider.Contacts.People
import android.provider.Contacts.Phones
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegateImpl.Companion.PEOPLE_PROJECTION
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegateImpl.Companion.PHONES_PROJECTION
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegateImpl.Companion.POSTAL_PROJECTION
import com.android.contacts.data.contacts.delegate.QueryLegacyContactsRepositoryDelegateImpl.Companion.POSTAL_SELECTION
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.data.tests.givenQueryResult
import com.android.contacts.data.tests.givenQueryRows
import com.android.contacts.data.tests.givenRegisteredContentObserver
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QueryLegacyContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = QueryLegacyContactsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryLegacyContacts_people_queriesPeopleUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).first()

        val query = queries.single()
        assertEquals(People.CONTENT_URI, query.uri)
        assertArrayEquals(PEOPLE_PROJECTION, query.projection)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
        assertEquals(People.DISPLAY_NAME, query.sortOrder)
    }

    @Test
    fun queryLegacyContacts_phones_queriesPhonesUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryLegacyContacts(LegacyContactsKind.PHONES, null).first()

        val query = queries.single()
        assertEquals(Phones.CONTENT_URI, query.uri)
        assertArrayEquals(PHONES_PROJECTION, query.projection)
        assertNull(query.selection)
        assertEquals(People.DISPLAY_NAME, query.sortOrder)
    }

    @Test
    fun queryLegacyContacts_postalAddresses_queriesPostalContactMethods() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryLegacyContacts(LegacyContactsKind.POSTAL_ADDRESSES, null).first()

        val query = queries.single()
        assertEquals(ContactMethods.CONTENT_URI, query.uri)
        assertArrayEquals(POSTAL_PROJECTION, query.projection)
        assertEquals(POSTAL_SELECTION, query.selection)
        assertNull(query.selectionArgs)
        assertEquals(People.DISPLAY_NAME, query.sortOrder)
    }

    @Test
    fun queryLegacyContacts_people_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", 1),
            arrayOf(2L, null, 0),
        )

        val result = subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).first()

        assertEquals(
            listOf(
                LegacyContactColumn(
                    id = 1L,
                    kind = LegacyContactsKind.PEOPLE,
                    displayName = "Alice",
                    isStarred = true,
                ),
                LegacyContactColumn(id = 2L, kind = LegacyContactsKind.PEOPLE),
            ),
            result,
        )
    }

    @Test
    fun queryLegacyContacts_phones_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, Phones.TYPE_MOBILE, null, "2025550123", "Alice"),
            arrayOf(2L, null, "Custom", "2125550198", "Bob"),
        )

        val result = subject.queryLegacyContacts(LegacyContactsKind.PHONES, null).first()

        assertEquals(
            listOf(
                LegacyContactColumn(
                    id = 1L,
                    kind = LegacyContactsKind.PHONES,
                    displayName = "Alice",
                    value = "2025550123",
                    type = Phones.TYPE_MOBILE,
                ),
                LegacyContactColumn(
                    id = 2L,
                    kind = LegacyContactsKind.PHONES,
                    displayName = "Bob",
                    value = "2125550198",
                    label = "Custom",
                ),
            ),
            result,
        )
    }

    @Test
    fun queryLegacyContacts_postalAddresses_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, ContactMethods.TYPE_HOME, null, "1 Main Street, Springfield", "Alice"),
        )

        val result = subject.queryLegacyContacts(LegacyContactsKind.POSTAL_ADDRESSES, null)
            .first()

        assertEquals(
            listOf(
                LegacyContactColumn(
                    id = 1L,
                    kind = LegacyContactsKind.POSTAL_ADDRESSES,
                    displayName = "Alice",
                    value = "1 Main Street, Springfield",
                    type = ContactMethods.TYPE_HOME,
                ),
            ),
            result,
        )
    }

    @Test
    fun queryLegacyContacts_withSearchQuery_filtersByDisplayNameIgnoringCase() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Anna Smith", 0),
            arrayOf(2L, "Bob", 0),
            arrayOf(3L, "Joanna", 0),
            arrayOf(4L, null, 0),
        )

        val result = subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, "ANNA").first()

        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test
    fun queryLegacyContacts_withoutSearchQuery_returnsAllRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", 0),
            arrayOf(2L, null, 0),
        )

        val result = subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).first()

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun queryLegacyContacts_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryLegacyContacts(LegacyContactsKind.PHONES, null).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryLegacyContacts_observesContactsAuthority() = runTest {
        val uriSlot = slot<Uri>()
        contentResolver.givenRegisteredContentObserver(uriSlot)
        contentResolver.givenQueryRows()

        subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).first()

        assertEquals(ContactsContract.AUTHORITY_URI, uriSlot.captured)
    }

    @Test
    fun queryLegacyContacts_whenContactsChange_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(arrayOf(1L, "Alice", 0))

        subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).test {
            assertEquals(1L, awaitItem().single().id)

            contentResolver.givenQueryRows(arrayOf(2L, "Bob", 0))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryLegacyContacts_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows()

        subject.queryLegacyContacts(LegacyContactsKind.PEOPLE, null).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }
}
