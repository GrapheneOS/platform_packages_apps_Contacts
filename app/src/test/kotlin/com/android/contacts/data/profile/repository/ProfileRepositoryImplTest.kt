package com.android.contacts.data.profile.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.DisplayNameSources
import android.provider.ContactsContract.Profile
import app.cash.turbine.test
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.preference.ContactsPreferences
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryImplTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val contactsPreferences = mockk<ContactsPreferences>(relaxed = true)
    private val projectionSlot = slot<Array<String>>()

    private val repository = ProfileRepositoryImpl(
        contentResolver = contentResolver,
        contactsPreferences = contactsPreferences,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every { contactsPreferences.displayOrder } returns ContactsPreferences.DISPLAY_ORDER_PRIMARY
        givenProfileRows()
    }

    @Test
    fun observeProfile_whenProfileExists_emitsProfileData() = runTest {
        givenProfileRows(
            profileRow(
                contactId = 42L,
                displayName = "Anna Smith",
                isUserProfile = 1,
                displayNameSource = DisplayNameSources.STRUCTURED_NAME,
            ),
        )

        repository.observeProfile().test {
            assertEquals(
                ProfileData(
                    hasProfile = true,
                    contactId = 42L,
                    displayName = "Anna Smith",
                    isDisplayNameFromPhoneNumber = false,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenThereIsNoProfileRow_emitsEmptyProfileData() = runTest {
        givenProfileRows()

        repository.observeProfile().test {
            assertEquals(ProfileData(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenRowIsNotTheUserProfile_reportsNoProfileButKeepsContactId() = runTest {
        givenProfileRows(profileRow(contactId = 7L, isUserProfile = 0))

        repository.observeProfile().test {
            val profile = awaitItem()

            assertFalse(profile.hasProfile)
            assertEquals(7L, profile.contactId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenDisplayNameComesFromPhoneNumber_marksItAsPhoneNumber() = runTest {
        givenProfileRows(profileRow(displayNameSource = DisplayNameSources.PHONE))

        repository.observeProfile().test {
            assertTrue(awaitItem().isDisplayNameFromPhoneNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenProfileHasNoName_emitsNullDisplayName() = runTest {
        givenProfileRows(profileRow(displayName = null))

        repository.observeProfile().test {
            assertNull(awaitItem().displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenProviderReturnsNoCursor_emitsEmptyProfileData() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository.observeProfile().test {
            assertEquals(ProfileData(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenProviderFails_emitsEmptyProfileData() = runTest {
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } throws IllegalStateException("provider is down")

        repository.observeProfile().test {
            assertEquals(ProfileData(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenDisplayOrderIsPrimary_queriesPrimaryDisplayName() = runTest {
        every { contactsPreferences.displayOrder } returns ContactsPreferences.DISPLAY_ORDER_PRIMARY

        repository.observeProfile().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertArrayEquals(
            arrayOf(
                Contacts._ID,
                Contacts.DISPLAY_NAME_PRIMARY,
                Contacts.IS_USER_PROFILE,
                Contacts.DISPLAY_NAME_SOURCE,
            ),
            projectionSlot.captured,
        )
    }

    @Test
    fun observeProfile_whenDisplayOrderIsAlternative_queriesAlternativeDisplayName() = runTest {
        every {
            contactsPreferences.displayOrder
        } returns ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE

        repository.observeProfile().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(Contacts.DISPLAY_NAME_ALTERNATIVE, projectionSlot.captured[1])
    }

    @Test
    fun observeProfile_whenProfileChanges_emitsAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()
        givenProfileRows(profileRow(displayName = "Anna Smith"))

        repository.observeProfile().test {
            assertEquals("Anna Smith", awaitItem().displayName)

            givenProfileRows(profileRow(displayName = "Anna Jones"))
            observerSlot.captured.onChange(false)

            assertEquals("Anna Jones", awaitItem().displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeProfile_whenCollectionStops_unregistersObserver() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        repository.observeProfile().test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify { contentResolver.unregisterContentObserver(observerSlot.captured) }
    }

    private fun givenRegisteredContentObserver(): CapturingSlot<ContentObserver> {
        val observerSlot = slot<ContentObserver>()
        every {
            contentResolver.registerContentObserver(
                Profile.CONTENT_URI,
                true,
                capture(observerSlot),
            )
        } returns Unit
        return observerSlot
    }

    private fun givenProfileRows(vararg rows: Array<Any?>) {
        every {
            contentResolver.query(
                Profile.CONTENT_URI,
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

    private fun profileRow(
        contactId: Long = 1L,
        displayName: String? = "Anna Smith",
        isUserProfile: Int = 1,
        displayNameSource: Int = DisplayNameSources.STRUCTURED_NAME,
    ): Array<Any?> {
        return arrayOf(contactId, displayName, isUserProfile, displayNameSource)
    }
}
