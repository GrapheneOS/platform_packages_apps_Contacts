package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contacts.delegate.QueryDirectoryContactsRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.delegate.QueryDirectoryContactsRepositoryDelegateImpl.Companion.RESULT_LIMIT
import com.android.contacts.data.contacts.model.DirectoryContactColumn
import com.android.contacts.data.tests.givenQueryResult
import com.android.contacts.data.tests.givenQueryRows
import io.mockk.mockk
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
class QueryDirectoryContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = QueryDirectoryContactsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryDirectoryContacts_queriesFilterUriForDirectory() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryDirectoryContacts(directoryId = 7L, searchQuery = "alice b").first()

        val query = queries.single()
        assertEquals(
            Contacts.CONTENT_FILTER_URI.buildUpon()
                .appendPath("alice b")
                .appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, "7")
                .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, RESULT_LIMIT.toString())
                .build(),
            query.uri,
        )
        assertArrayEquals(PROJECTION, query.projection)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
        assertNull(query.sortOrder)
    }

    @Test
    fun queryDirectoryContacts_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice Smith", "Smith, Alice", "content://photo/1", "key-1"),
            arrayOf(2L, null, null, " ", "key-2"),
        )

        val result = subject.queryDirectoryContacts(7L, "alice").first()

        assertEquals(
            listOf(
                DirectoryContactColumn(
                    directoryId = 7L,
                    id = 1L,
                    lookupKey = "key-1",
                    displayNamePrimary = "Alice Smith",
                    displayNameAlternative = "Smith, Alice",
                    photo = ContactPhoto.Uri("content://photo/1"),
                ),
                DirectoryContactColumn(directoryId = 7L, id = 2L, lookupKey = "key-2"),
            ),
            result,
        )
    }

    @Test
    fun queryDirectoryContacts_skipsRowsWithoutLookupKey() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", null, null, null),
            arrayOf(2L, "Bob", null, null, " "),
            arrayOf(3L, "Carol", null, null, "key-3"),
        )

        val result = subject.queryDirectoryContacts(7L, "a").first()

        assertEquals(listOf(3L), result.map { it.id })
    }

    @Test
    fun queryDirectoryContacts_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryDirectoryContacts(7L, "alice").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryDirectoryContacts_whenQueryFails_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { throw IllegalStateException("Directory unavailable") }

        val result = subject.queryDirectoryContacts(7L, "alice").first()

        assertTrue(result.isEmpty())
    }
}
