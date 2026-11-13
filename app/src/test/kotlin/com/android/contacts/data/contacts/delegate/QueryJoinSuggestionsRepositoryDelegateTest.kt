package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Contacts.AggregationSuggestions
import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.delegate.QueryJoinSuggestionsRepositoryDelegateImpl.Companion.MAX_SUGGESTIONS
import com.android.contacts.data.contacts.model.ContactColumn
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
class QueryJoinSuggestionsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = QueryJoinSuggestionsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryJoinSuggestions_withoutSearchQuery_queriesSuggestionsUri() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryJoinSuggestions(targetContactId = 5L, searchQuery = null).first()

        val query = queries.single()
        assertEquals(
            Uri.parse(
                "content://${ContactsContract.AUTHORITY}/contacts/5/" +
                    "${AggregationSuggestions.CONTENT_DIRECTORY}?" +
                    "${ContactsContract.LIMIT_PARAM_KEY}=$MAX_SUGGESTIONS",
            ),
            query.uri,
        )
        assertArrayEquals(PROJECTION, query.projection)
        assertNull(query.selection)
        assertNull(query.selectionArgs)
        assertNull(query.sortOrder)
    }

    @Test
    fun queryJoinSuggestions_withSearchQuery_appendsEncodedQueryToPath() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryJoinSuggestions(targetContactId = 5L, searchQuery = "alice b").first()

        val uri = queries.single().uri
        assertEquals(
            listOf("contacts", "5", AggregationSuggestions.CONTENT_DIRECTORY, "alice b"),
            uri.pathSegments,
        )
        assertEquals("alice%20b", uri.encodedPath!!.substringAfterLast('/'))
        assertEquals(
            MAX_SUGGESTIONS.toString(),
            uri.getQueryParameter(ContactsContract.LIMIT_PARAM_KEY),
        )
    }

    @Test
    fun queryJoinSuggestions_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice Smith", "Smith, Alice", "content://photo/1", 1),
            arrayOf(2L, null, null, null, 0),
        )

        val result = subject.queryJoinSuggestions(5L, null).first()

        assertEquals(
            listOf(
                ContactColumn(
                    id = 1L,
                    displayNamePrimary = "Alice Smith",
                    displayNameAlternative = "Smith, Alice",
                    photo = ContactPhoto.Uri("content://photo/1"),
                    isStarred = true,
                ),
                ContactColumn(id = 2L),
            ),
            result,
        )
    }

    @Test
    fun queryJoinSuggestions_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryJoinSuggestions(5L, null).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryJoinSuggestions_observesContactsUri() = runTest {
        val uriSlot = slot<Uri>()
        contentResolver.givenRegisteredContentObserver(uriSlot)
        contentResolver.givenQueryRows()

        subject.queryJoinSuggestions(5L, null).first()

        assertEquals(Contacts.CONTENT_URI, uriSlot.captured)
    }

    @Test
    fun queryJoinSuggestions_whenContactsChange_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(arrayOf(1L, "Alice", null, null, 0))

        subject.queryJoinSuggestions(5L, null).test {
            assertEquals(1L, awaitItem().single().id)

            contentResolver.givenQueryRows(arrayOf(2L, "Bob", null, null, 0))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryJoinSuggestions_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows()

        subject.queryJoinSuggestions(5L, null).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }
}
