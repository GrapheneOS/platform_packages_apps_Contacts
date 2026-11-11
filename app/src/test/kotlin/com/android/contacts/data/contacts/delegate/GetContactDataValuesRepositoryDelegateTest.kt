package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Data
import com.android.contacts.data.contacts.delegate.GetContactDataValuesRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.tests.givenQueryResult
import com.android.contacts.data.tests.givenQueryRows
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class GetContactDataValuesRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = GetContactDataValuesRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun getContactDataValues_withoutIds_returnsEmptyListWithoutQuerying() = runTest {
        val result = subject.getContactDataValues(ContactDataKind.Email, emptySet())

        assertTrue(result.isEmpty())
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getContactDataValues_queriesDataByMimeTypeAndIds() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getContactDataValues(ContactDataKind.Email, setOf(1L, 2L, 3L))

        val query = queries.single()
        assertEquals(Data.CONTENT_URI, query.uri)
        assertArrayEquals(PROJECTION, query.projection)
        assertEquals("${Data.MIMETYPE} = ? AND ${Data._ID} IN (1,2,3)", query.selection)
        assertArrayEquals(arrayOf(Email.CONTENT_ITEM_TYPE), query.selectionArgs)
        assertNull(query.sortOrder)
    }

    @Test
    fun getContactDataValues_forPhone_usesPhoneMimeType() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getContactDataValues(ContactDataKind.Phone(), setOf(1L))

        assertEquals(Phone.CONTENT_ITEM_TYPE, queries.single().selectionArgs!!.first())
    }

    @Test
    fun getContactDataValues_forCallable_usesPhoneMimeType() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getContactDataValues(ContactDataKind.Phone(callableOnly = true), setOf(1L))

        assertEquals(Phone.CONTENT_ITEM_TYPE, queries.single().selectionArgs!!.first())
    }

    @Test
    fun getContactDataValues_forPostalAddress_usesPostalMimeType() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getContactDataValues(ContactDataKind.PostalAddress, setOf(1L))

        assertEquals(StructuredPostal.CONTENT_ITEM_TYPE, queries.single().selectionArgs!!.first())
    }

    @Test
    fun getContactDataValues_returnsValuesSkippingNullAndBlank() = runTest {
        contentResolver.givenQueryRows(
            arrayOf("a@example.org"),
            arrayOf(null),
            arrayOf("  "),
            arrayOf("b@example.org"),
        )

        val result = subject.getContactDataValues(ContactDataKind.Email, setOf(1L, 2L, 3L, 4L))

        assertEquals(listOf("a@example.org", "b@example.org"), result)
    }

    @Test
    fun getContactDataValues_whenCursorIsNull_returnsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.getContactDataValues(ContactDataKind.Email, setOf(1L))

        assertTrue(result.isEmpty())
    }
}
