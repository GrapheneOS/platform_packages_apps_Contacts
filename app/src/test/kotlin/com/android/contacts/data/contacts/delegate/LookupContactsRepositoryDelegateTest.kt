package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.content.ContentUris
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
class LookupContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val uriSlot = slot<Uri>()
    private val projectionSlot = slot<Array<String>>()

    private val subject = LookupContactsRepositoryDelegateImpl(
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun lookup_whenContactMatches_emitsResult() = runTest {
        givenQueryRows(
            resultRow(1L, "1"),
            resultRow(2L, "2"),
        )

        subject.lookup(ContactLookupQuery.Email("user@example.org")).test {
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

        subject.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertTrue(awaitItem()!!.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenNoContactMatches_emitsResult() = runTest {
        givenQueryRows()

        subject.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertTrue(awaitItem()!!.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenQueryByEmail_usesCorrespodingUriAndProjection() = runTest {
        givenQueryRows()
        val email = "user@example.org"

        subject.lookup(ContactLookupQuery.Email(email)).first()

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

        subject.lookup(ContactLookupQuery.Phone(phone)).first()

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
    fun lookup_whenQueryById_usesCorrespodingUriAndProjection() = runTest {
        givenQueryRows()

        subject.lookup(ContactLookupQuery.Id(42L)).first()

        assertEquals(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, 42L),
            uriSlot.captured,
        )
        assertArrayEquals(
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
            ),
            projectionSlot.captured,
        )
    }

    @Test
    fun lookup_whenQueryById_emitsResult() = runTest {
        givenQueryRows(resultRow(42L, "key"))

        val result = subject.lookup(ContactLookupQuery.Id(42L)).first()

        assertEquals(
            listOf(
                ContactLookupResult(
                    42L,
                    "key",
                    ContactsContract.Contacts.getLookupUri(42L, "key"),
                ),
            ),
            result,
        )
    }

    @Test
    fun lookup_observesQueryUri() = runTest {
        val observedUriSlot = slot<Uri>()
        every {
            contentResolver.registerContentObserver(capture(observedUriSlot), true, any())
        } returns Unit
        givenQueryRows()

        subject.lookup(ContactLookupQuery.Id(42L)).first()

        assertEquals(uriSlot.captured, observedUriSlot.captured)
    }

    @Test
    fun lookup_whenContactChanges_emitsAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()
        givenQueryRows(resultRow(1L, "1"))

        subject.lookup(ContactLookupQuery.Email("user@example.org")).test {
            assertEquals(1L, awaitItem()!!.first().id)

            givenQueryRows(resultRow(2L, "2"))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem()!!.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun lookup_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        subject.lookup(ContactLookupQuery.Email("user@example.org")).test {
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
