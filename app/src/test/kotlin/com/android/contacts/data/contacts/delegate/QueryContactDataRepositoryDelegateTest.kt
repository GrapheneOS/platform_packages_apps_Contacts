package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Callable
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contacts.delegate.QueryContactDataRepositoryDelegateImpl.Companion.IGNORE_NUMBER_TOO_LONG_CLAUSE
import com.android.contacts.data.contacts.delegate.QueryContactDataRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.tests.addressBookIndexExtras
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
class QueryContactDataRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = QueryContactDataRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryContactData_phoneWithoutSearchQuery_queriesPhoneContentUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Phone(), null, SortOrder.GIVEN_NAME_FIRST)
            .first()

        val query = queries.single()
        assertEquals(contentUriWithIndex(Phone.CONTENT_URI), query.uri)
        assertArrayEquals(PROJECTION, query.projection)
        assertEquals(IGNORE_NUMBER_TOO_LONG_CLAUSE, query.selection)
        assertNull(query.selectionArgs)
        assertEquals(Contacts.SORT_KEY_PRIMARY, query.sortOrder)
    }

    @Test
    fun queryContactData_phoneWithSearchQuery_queriesPhoneFilterUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Phone(), "alice", SortOrder.GIVEN_NAME_FIRST)
            .first()

        assertEquals(

            filterUri(Phone.ENTERPRISE_CONTENT_FILTER_URI, "alice"),
            queries.single().uri,
        )
    }

    @Test
    fun queryContactData_callableWithoutSearchQuery_queriesCallableContentUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(
            kind = ContactDataKind.Phone(callableOnly = true),
            searchQuery = null,
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
        ).first()

        val query = queries.single()
        assertEquals(contentUriWithIndex(Callable.CONTENT_URI), query.uri)
        assertEquals(IGNORE_NUMBER_TOO_LONG_CLAUSE, query.selection)
    }

    @Test
    fun queryContactData_callableWithSearchQuery_queriesCallableFilterUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(
            kind = ContactDataKind.Phone(callableOnly = true),
            searchQuery = "alice",
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
        ).first()

        assertEquals(
            filterUri(Callable.ENTERPRISE_CONTENT_FILTER_URI, "alice"),
            queries.single().uri,
        )
    }

    @Test
    fun queryContactData_emailWithoutSearchQuery_queriesEmailContentUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Email, null, SortOrder.GIVEN_NAME_FIRST).first()

        val query = queries.single()
        assertEquals(contentUriWithIndex(Email.CONTENT_URI), query.uri)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContactData_emailWithSearchQuery_queriesEmailFilterUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Email, "alice", SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(
            filterUri(Email.CONTENT_FILTER_URI, "alice"),
            queries.single().uri,
        )
    }

    @Test
    fun queryContactData_postalAddressWithoutSearchQuery_queriesPostalContentUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.PostalAddress, null, SortOrder.GIVEN_NAME_FIRST)
            .first()

        val query = queries.single()
        assertEquals(
            contentUriWithIndex(StructuredPostal.CONTENT_URI),
            query.uri,
        )
        assertNull(query.selection)
    }

    @Test
    fun queryContactData_postalAddressWithSearchQuery_ignoresSearchQuery() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.PostalAddress, "alice", SortOrder.GIVEN_NAME_FIRST)
            .first()

        assertEquals(
            contentUriWithIndex(StructuredPostal.CONTENT_URI),
            queries.single().uri,
        )
    }

    @Test
    fun queryContactData_withFamilyNameFirst_sortsByAlternativeKey() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Email, null, SortOrder.FAMILY_NAME_FIRST).first()

        assertEquals(
            Contacts.SORT_KEY_ALTERNATIVE,
            queries.single().sortOrder
        )
    }

    @Test
    fun queryContactData_phoneRestrictedToContacts_combinesSelections() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(
            kind = ContactDataKind.Phone(),
            searchQuery = null,
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            restrictToContactIds = setOf(1L, 2L),
        ).first()

        val query = queries.single()
        assertEquals(
            "$IGNORE_NUMBER_TOO_LONG_CLAUSE AND ${Data.CONTACT_ID} IN (1,2)",
            query.selection,
        )
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContactData_emailRestrictedToContacts_selectsContacts() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryContactData(
            kind = ContactDataKind.Email,
            searchQuery = null,
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            restrictToContactIds = setOf(3L),
        ).first()

        val query = queries.single()
        assertEquals("${Data.CONTACT_ID} IN (3)", query.selection)
        assertNull(query.selectionArgs)
    }

    @Test
    fun queryContactData_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(
                10L,
                "Alice Smith",
                "Smith, Alice",
                "content://photo/1",
                1,
                "+1 202-555-0123",
                Phone.TYPE_MOBILE,
                null,
            ),
            arrayOf(11L, null, null, null, 0, null, null, "Custom"),
        )
        val kind = ContactDataKind.Phone()

        val result = subject.queryContactData(kind, null, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(
            listOf(
                ContactDataColumn(
                    id = 10L,
                    kind = kind,
                    displayNamePrimary = "Alice Smith",
                    displayNameAlternative = "Smith, Alice",
                    photo = ContactPhoto.Uri("content://photo/1"),
                    value = "+1 202-555-0123",
                    type = Phone.TYPE_MOBILE,
                    isStarred = true,
                ),
                ContactDataColumn(
                    id = 11L,
                    kind = kind,
                    label = "Custom",
                ),
            ),
            result,
        )
    }

    @Test
    fun queryContactData_withAddressBookIndex_mapsSectionLabels() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", null, null, 0, "a@example.org", null, null),
            arrayOf(2L, "Bob", null, null, 0, "b@example.org", null, null),
            extras = addressBookIndexExtras(arrayOf("A", "B"), intArrayOf(1, 1)),
        )

        val result = subject.queryContactData(
            ContactDataKind.Email,
            null,
            SortOrder.GIVEN_NAME_FIRST,
        ).first()

        assertEquals(listOf("A", "B"), result.map { it.sectionLabel })
    }

    @Test
    fun queryContactData_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryContactData(
            ContactDataKind.Email,
            null,
            SortOrder.GIVEN_NAME_FIRST,
        ).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryContactData_observesDataUri() = runTest {
        val uriSlot = slot<Uri>()
        contentResolver.givenRegisteredContentObserver(uriSlot)
        contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Email, null, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(Data.CONTENT_URI, uriSlot.captured)
    }

    @Test
    fun queryContactData_whenDataChanges_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(arrayOf(1L, "Alice", null, null, 0, "a", null, null))

        subject.queryContactData(ContactDataKind.Email, null, SortOrder.GIVEN_NAME_FIRST).test {
            assertEquals(1L, awaitItem().single().id)

            contentResolver.givenQueryRows(arrayOf(2L, "Bob", null, null, 0, "b", null, null))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryContactData_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows()

        subject.queryContactData(ContactDataKind.Email, null, SortOrder.GIVEN_NAME_FIRST).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }

    private fun contentUriWithIndex(contentUri: Uri): Uri {
        return contentUri.buildUpon()
            .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
            .appendDefaultDirectoryAndRemoveDuplicates()
            .build()
    }

    private fun filterUri(filterUri: Uri, searchQuery: String): Uri {
        return filterUri.buildUpon()
            .appendPath(searchQuery)
            .appendDefaultDirectoryAndRemoveDuplicates()
            .build()
    }

    private fun Uri.Builder.appendDefaultDirectoryAndRemoveDuplicates(): Uri.Builder {
        return appendQueryParameter(
            ContactsContract.DIRECTORY_PARAM_KEY,
            ContactsContract.Directory.DEFAULT.toString(),
        ).appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
    }
}
