package com.android.contacts.data.contacts.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.model.ContactLookupResult
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ContactsRepositoryImplTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val uriSlot = slot<Uri>()
    private val projectionSlot = slot<Array<String>>()

    private val repository = ContactsRepositoryImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun lookup_whenContactMatches_emitsResult() = runTest {
        givenQueryRows(
            resultRow(1L, "1"),
            resultRow(2L, "2"),
        )

        repository.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertEquals(
                listOf(
                    ContactLookupResult(
                        1L,
                        "1",
                        ContactsContract.Contacts.getLookupUri(1L, "1"),
                    ),
                    ContactLookupResult(
                        2L,
                        "2",
                        ContactsContract.Contacts.getLookupUri(2L, "2"),
                    ),
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenKeyIsNull_skipsResult() = runTest {
        givenQueryRows(
            resultRow(2L, null),
        )

        repository.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenNoContactMatches_emitsResult() = runTest {
        givenQueryRows()

        repository.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenQueryByEmail_usesCorrespodingUriAndProjection() = runTest {
        givenQueryRows()
        val email = "user@example.org"

        repository.lookup(ContactLookupQuery.Email(email)).first()

        assertEquals(
            Uri.withAppendedPath(
                ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
                Uri.encode(email),
            ),
            uriSlot.captured,
        )
        assertArrayEquals(
            arrayOf(
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.LOOKUP_KEY,
            ),
            projectionSlot.captured,
        )
    }

    @Test
    fun lookup_whenQueryByPhone_usesCorrespodingUriAndProjection() = runTest {
        givenQueryRows()
        val phone = "123456789"

        repository.lookup(ContactLookupQuery.Phone(phone)).first()

        assertEquals(
            Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone),
            ),
            uriSlot.captured,
        )
        assertArrayEquals(
            arrayOf(
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.LOOKUP_KEY,
            ),
            projectionSlot.captured,
        )
    }

    @Test
    fun lookup_whenContactChanges_emitsAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()
        givenQueryRows(resultRow(1L, "1"))

        repository.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertEquals(1L, awaitItem().first().id)

            givenQueryRows(resultRow(2L, "2"))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        repository.lookup(ContactLookupQuery.Email("user@example.org")).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
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

    private fun givenQueryRows(vararg rows: Array<Any?>) {
        every {
            contentResolver.query(
                capture(uriSlot),
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

    private fun resultRow(
        contactId: Long = 1L,
        contactKey: String? = "1",
    ): Array<Any?> {
        return arrayOf(contactId, contactKey)
    }
}
