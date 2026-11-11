package com.android.contacts.data.contacts.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract.Directory
import app.cash.turbine.test
import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.data.contacts.repository.DirectoriesRepositoryImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.repository.DirectoriesRepositoryImpl.Companion.SELECTION
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
class DirectoriesRepositoryTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject = DirectoriesRepositoryImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun observeRemote_queriesDirectoriesWithFullShortcutSupport() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.observeRemote().first()

        val query = queries.single()
        assertEquals(Directory.CONTENT_URI, query.uri)
        assertArrayEquals(PROJECTION, query.projection)
        assertEquals(SELECTION, query.selection)
        assertNull(query.selectionArgs)
        assertEquals(Directory._ID, query.sortOrder)
    }

    @Test
    fun observeRemote_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            directoryRow(10L, "com.example.exchange", 123, "Corporate Directory"),
            directoryRow(11L, null, 0, null),
        )

        val result = subject.observeRemote().first()

        assertEquals(
            listOf(
                DirectoryColumn(
                    id = 10L,
                    displayName = "Corporate Directory",
                    packageName = "com.example.exchange",
                    typeResourceId = 123,
                ),
                DirectoryColumn(id = 11L),
            ),
            result,
        )
    }

    @Test
    fun observeRemote_skipsNonRemoteDirectories() = runTest {
        contentResolver.givenQueryRows(
            directoryRow(Directory.DEFAULT),
            directoryRow(Directory.LOCAL_INVISIBLE),
            directoryRow(Directory.ENTERPRISE_DEFAULT),
            directoryRow(Directory.ENTERPRISE_LOCAL_INVISIBLE),
            directoryRow(10L),
        )

        val result = subject.observeRemote().first()

        assertEquals(listOf(10L), result.map { it.id })
    }

    @Test
    fun observeRemote_withBlankDisplayName_mapsToNull() = runTest {
        contentResolver.givenQueryRows(directoryRow(10L, displayName = "  "))

        val result = subject.observeRemote().first()

        assertNull(result.single().displayName)
    }

    @Test
    fun observeRemote_withEmptyPackageName_mapsToNull() = runTest {
        contentResolver.givenQueryRows(directoryRow(10L, packageName = ""))

        val result = subject.observeRemote().first()

        assertNull(result.single().packageName)
    }

    @Test
    fun observeRemote_withZeroTypeResourceId_mapsToNull() = runTest {
        contentResolver.givenQueryRows(directoryRow(10L, typeResourceId = 0))

        val result = subject.observeRemote().first()

        assertNull(result.single().typeResourceId)
    }

    @Test
    fun observeRemote_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.observeRemote().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeRemote_whenQueryFails_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { throw IllegalStateException("Provider unavailable") }

        val result = subject.observeRemote().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeRemote_observesDirectoriesUri() = runTest {
        val uriSlot = slot<Uri>()
        contentResolver.givenRegisteredContentObserver(uriSlot)
        contentResolver.givenQueryRows()

        subject.observeRemote().first()

        assertEquals(Directory.CONTENT_URI, uriSlot.captured)
    }

    @Test
    fun observeRemote_whenDirectoriesChange_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(directoryRow(10L))

        subject.observeRemote().test {
            assertEquals(10L, awaitItem().single().id)

            contentResolver.givenQueryRows(directoryRow(11L))
            observerSlot.captured.onChange(false)

            assertEquals(11L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeRemote_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows()

        subject.observeRemote().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }

    private fun directoryRow(
        id: Long,
        packageName: String? = "com.example.directory",
        typeResourceId: Int = 1,
        displayName: String? = "Directory",
    ): Array<Any?> {
        return arrayOf(id, packageName, typeResourceId, displayName)
    }
}
