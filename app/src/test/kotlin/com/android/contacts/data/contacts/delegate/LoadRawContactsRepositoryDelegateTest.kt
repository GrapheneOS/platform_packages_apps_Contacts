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
    fun withContactUri_doesNotThrowException() = runTest {
        subject.loadRawContacts(CONTACT_URI).first()
    }

    @Test
    fun withProfileUri_doesNotThrowException() = runTest {
        subject.loadRawContacts(PROFILE_URI).first()
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

        verifyQuery(CONTACT_URI)
        verifyQuery(ContactsContract.RawContacts.CONTENT_URI, exactly = 0)
        verifyQuery(ContactsContract.Data.CONTENT_URI, exactly = 0)
    }

    @Test
    fun whenRawContactsAreMissing_returnNull() = runTest {
        givenQueryRows(CONTACT_URI, arrayOf(1L, 0))
        givenQueryRows(ContactsContract.RawContacts.CONTENT_URI)
        assertNull(subject.loadRawContacts(CONTACT_URI).first())

        verifyQuery(CONTACT_URI)
        verifyQuery(ContactsContract.RawContacts.CONTENT_URI)
        verifyQuery(ContactsContract.Data.CONTENT_URI, exactly = 0)
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
        givenQueryRows(CONTACT_URI, arrayOf(1L, 0))
        givenQueryRows(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(1, "Person 1", "Person 1 Alt", "Account", "Device", null),
            arrayOf(2, "Person 2", "Person 2 Alt", "Account", "Device", null),
        )
        givenQueryRows(ContactsContract.Data.CONTENT_URI, arrayOf(1, "content://photos/1"))

        val result = subject.loadRawContacts(CONTACT_URI).first()!!
        with(result.rawContacts[0]) {
            assertEquals(1L, id)
            assertEquals("content://photos/1", photoUri)
        }
        with(result.rawContacts[1]) {
            assertEquals(2L, id)
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

        verifyQuery(CONTACT_URI, exactly = 2)
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

    private fun verifyQuery(uri: Uri, exactly: Int = 1) {
        verify(exactly = exactly) {
            contentResolver.query(uri, any(), any(), any(), any())
        }
    }

    private companion object {
        val CONTACT_URI: Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "1")
        val PROFILE_URI: Uri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, "1")
    }
}
