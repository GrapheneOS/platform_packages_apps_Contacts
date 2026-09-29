package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.database.EmptyCursor
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class LoadRawContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject: LoadRawContactsRepositoryDelegate = LoadRawContactsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns EmptyCursor(emptyArray())
    }

    @Test(expected = IllegalArgumentException::class)
    fun withoutContactOrProfileUri_throwsIllegalArgumentException() = runTest {
        subject.loadRawContacts(Uri.parse("https://invalid.org"))
            .first()
    }

    @Test
    fun withContactUri_runsCorrectQueries() = runTest {
        val rawContactId = 1L
        givenQueryRows(CONTACT_URI, arrayOf(rawContactId, 0))
        givenQueryRows(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(rawContactId, "Person", "Person Alt", "Account", "Device", null),
        )

        subject.loadRawContacts(CONTACT_URI).first()

        verify {
            contentResolver.query(
                CONTACT_URI,
                LoadRawContactsRepositoryDelegateImpl.PROFILE_PROJECTION,
                null,
                null,
                null,
            )
        }
        verify {
            contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                LoadRawContactsRepositoryDelegateImpl.RAW_CONTACT_PROJECTION,
                LoadRawContactsRepositoryDelegateImpl.RAW_CONTACT_SELECTION,
                arrayOf(rawContactId.toString()),
                null,
            )
        }
        verify {
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                LoadRawContactsRepositoryDelegateImpl.PHOTO_PROJECTION,
                LoadRawContactsRepositoryDelegateImpl.PHOTO_SELECTION_PREFIX +
                    rawContactId +
                    LoadRawContactsRepositoryDelegateImpl.PHOTO_SELECTION_SUFFIX,
                null,
                null,
            )
        }
    }

    @Test
    fun withProfileUri_runsCorrectQueries() = runTest {
        val rawContactId = 1L
        givenQueryRows(PROFILE_URI, arrayOf(rawContactId, 1))
        givenQueryRows(
            ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
            arrayOf(rawContactId, "Person", "Person Alt", "Account", "Device", null),
        )

        subject.loadRawContacts(PROFILE_URI).first()

        verify {
            contentResolver.query(
                PROFILE_URI,
                LoadRawContactsRepositoryDelegateImpl.PROFILE_PROJECTION,
                null,
                null,
                null,
            )
        }
        verify {
            contentResolver.query(
                ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
                LoadRawContactsRepositoryDelegateImpl.RAW_CONTACT_PROJECTION,
                LoadRawContactsRepositoryDelegateImpl.RAW_CONTACT_SELECTION,
                arrayOf(rawContactId.toString()),
                null,
            )
        }
        verify {
            contentResolver.query(
                Uri.withAppendedPath(
                    ContactsContract.Profile.CONTENT_URI,
                    ContactsContract.Data.CONTENT_URI.path,
                ),
                LoadRawContactsRepositoryDelegateImpl.PHOTO_PROJECTION,
                LoadRawContactsRepositoryDelegateImpl.PHOTO_SELECTION_PREFIX +
                    rawContactId +
                    LoadRawContactsRepositoryDelegateImpl.PHOTO_SELECTION_SUFFIX,
                null,
                null,
            )
        }
    }

    @Test
    fun whenFlowIsCollectedAndTerminated_registersAndUnregistesObserver() = runTest {
        subject.loadRawContacts(CONTACT_URI).first()

        verify { contentResolver.registerContentObserver(CONTACT_URI, any(), any()) }
        verify { contentResolver.unregisterContentObserver(any()) }
    }

    @Test
    fun whenContactIsMissing_returnNull() = runTest {
        givenQueryRows(CONTACT_URI)
        assertNull(subject.loadRawContacts(CONTACT_URI).first())

        verifyQueryByUri(CONTACT_URI)
        verifyQueryByUri(ContactsContract.RawContacts.CONTENT_URI, exactly = 0)
        verifyQueryByUri(ContactsContract.Data.CONTENT_URI, exactly = 0)
    }

    @Test
    fun whenRawContactsAreMissing_returnNull() = runTest {
        givenQueryRows(CONTACT_URI, arrayOf(1L, 0))
        givenQueryRows(ContactsContract.RawContacts.CONTENT_URI)
        assertNull(subject.loadRawContacts(CONTACT_URI).first())

        verifyQueryByUri(CONTACT_URI)
        verifyQueryByUri(ContactsContract.RawContacts.CONTENT_URI)
        verifyQueryByUri(ContactsContract.Data.CONTENT_URI, exactly = 0)
    }

    @Test
    fun returnsCorrectProfileAndMetadata() = runTest {
        givenQueryRows(CONTACT_URI, arrayOf(1L, 1))
        givenQueryRows(
            ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
            arrayOf(1, "Person 1", "Person 1 Alt", "Account", "Device", null),
            arrayOf(2, "Person 2", "Person 2 Alt", "Account", "Device", null),
        )

        val result = subject.loadRawContacts(CONTACT_URI).first()!!

        assertEquals(1L, result.contactId)
        assertTrue(result.isUserProfile)
        with(result.rawContacts[0]) {
            assertEquals(1L, id)
            assertEquals("Person 1", displayName)
            assertEquals("Person 1 Alt", displayNameAlt)
            assertEquals("Account", accountName)
            assertEquals("Device", accountType)
            assertNull(accountDataSet)
        }
        with(result.rawContacts[1]) {
            assertEquals(2L, id)
            assertEquals("Person 2", displayName)
            assertEquals("Person 2 Alt", displayNameAlt)
            assertEquals("Account", accountName)
            assertEquals("Device", accountType)
            assertNull(accountDataSet)
        }
    }

    @Test
    fun returnsContactsWithPhotos() = runTest {
        givenQueryRows(CONTACT_URI, arrayOf(123L, 0))
        givenQueryRows(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(1, "Person 1", "Person 1 Alt", "Account", "Device", null),
            arrayOf(2, "Person 2", "Person 2 Alt", "Account", "Device", null),
            arrayOf(3, "Person 3", "Person 3 Alt", "Account", "Device", null),
        )
        givenQueryRows(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(1, 1001),
            arrayOf(2, 1002),
        )

        val result = subject.loadRawContacts(CONTACT_URI).first()!!
        with(result.rawContacts[0]) {
            assertEquals(1L, id)
            assertEquals(
                Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "1001").toString(),
                photoUri,
            )
        }
        with(result.rawContacts[1]) {
            assertEquals(2L, id)
            assertEquals(
                Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "1002").toString(),
                photoUri,
            )
        }
        with(result.rawContacts[2]) {
            assertEquals(3L, id)
            assertNull(photoUri)
        }
    }

    @Test
    fun whenObserverTriggers_loadsAndReturnsDataAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        subject.loadRawContacts(CONTACT_URI).test {
            awaitItem()
            observerSlot.captured.onChange(false)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verifyQueryByUri(CONTACT_URI, exactly = 2)
    }

    private fun givenRegisteredContentObserver(): CapturingSlot<ContentObserver> {
        val observerSlot = slot<ContentObserver>()
        every {
            contentResolver.registerContentObserver(
                any(),
                true,
                capture(observerSlot),
            )
        } returns Unit
        return observerSlot
    }

    private fun givenQueryRows(uri: Uri, vararg rows: Array<Any?>) {
        val projectionSlot = slot<Array<String>>()
        every {
            contentResolver.query(
                uri,
                capture(projectionSlot),
                any(),
                any(),
                any(),
            )
        } answers {
            MatrixCursor(projectionSlot.captured).apply {
                rows.forEach { addRow(it) }
            }
        }
    }

    private fun verifyQueryByUri(
        uri: Uri,
        exactly: Int = 1,
    ) {
        verify(exactly = exactly) { contentResolver.query(uri, any(), any(), any(), any()) }
    }

    private companion object {
        val CONTACT_URI: Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "123")
        val PROFILE_URI: Uri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, "234")
    }
}
