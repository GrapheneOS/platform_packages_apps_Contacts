package com.android.contacts.domain.contacts.usecase

import android.content.ContentUris
import android.provider.ContactsContract.Contacts
import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.LoadedContact
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.tests.factory.contactDetails
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GetContactTest {

    private val contactDetailsRepository = mockk<ContactDetailsRepository>(relaxed = true)
    private val displaySettingsRepository = mockk<DisplaySettingsRepository>()
    private val displaySettings = MutableStateFlow(DISPLAY_SETTINGS)
    private val contactItemMapper = mockk<ContactItemMapper>()

    private val subject: GetContact = GetContactImpl(
        contactDetailsRepository = contactDetailsRepository,
        displaySettingsRepository = displaySettingsRepository,
        contactItemMapper = contactItemMapper,
    )

    @Before
    fun setUp() {
        every { displaySettingsRepository.observeDisplaySettings() } returns displaySettings
        every { contactItemMapper.map(any<ContactDetails>(), any()) } answers {
            item(firstArg<ContactDetails>().contactId, secondArg())
        }
    }

    @Test
    fun invoke_observesContactUriWithoutExcludedMimeTypes() = runTest {
        every { contactDetailsRepository.observeContactDetails(any(), any()) } returns emptyFlow()

        subject(CONTACT_ID).test { expectNoEvents() }

        val expectedUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, CONTACT_ID)
        verify {
            contactDetailsRepository.observeContactDetails(expectedUri, emptySet())
        }
    }

    @Test
    fun invoke_whenLoaded_emitsMappedContactItem() = runTest {
        val details = contactDetails(contactId = CONTACT_ID)
        every {
            contactDetailsRepository.observeContactDetails(any(), any())
        } returns flowOf(loaded(details))

        assertEquals(
            item(CONTACT_ID, DisplayOrder.GIVEN_NAME_FIRST),
            subject(CONTACT_ID).first(),
        )
        verify { contactItemMapper.map(details, DisplayOrder.GIVEN_NAME_FIRST) }
    }

    @Test
    fun invoke_skipsNotFoundAndErrorResults() = runTest {
        val results = MutableSharedFlow<ContactDetailsResult>()
        every { contactDetailsRepository.observeContactDetails(any(), any()) } returns results

        subject(CONTACT_ID).test {
            results.emit(ContactDetailsResult.NotFound)
            results.emit(loaded(contactDetails(contactId = 1L)))
            results.emit(ContactDetailsResult.Error)
            results.emit(loaded(contactDetails(contactId = 2L)))

            assertEquals(item(1L, DisplayOrder.GIVEN_NAME_FIRST), awaitItem())
            assertEquals(item(2L, DisplayOrder.GIVEN_NAME_FIRST), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun invoke_whenDisplayOrderChanges_remapsContact() = runTest {
        every {
            contactDetailsRepository.observeContactDetails(any(), any())
        } returns flowOf(loaded(contactDetails(contactId = CONTACT_ID)))

        subject(CONTACT_ID).test {
            assertEquals(item(CONTACT_ID, DisplayOrder.GIVEN_NAME_FIRST), awaitItem())

            displaySettings.update { it.copy(sortOrder = SortOrder.FAMILY_NAME_FIRST) }
            expectNoEvents()

            displaySettings.update { it.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST) }
            assertEquals(item(CONTACT_ID, DisplayOrder.FAMILY_NAME_FIRST), awaitItem())
        }
    }

    private fun loaded(details: ContactDetails): ContactDetailsResult.Loaded {
        return ContactDetailsResult.Loaded(
            details = details,
            source = LoadedContact(mockk(), mockk(relaxed = true)),
        )
    }

    private fun item(id: Long, displayOrder: DisplayOrder): ContactItem {
        return ContactItem(key = ContactItemKey.Local(id), name = "$id-$displayOrder")
    }

    companion object {
        private const val CONTACT_ID = 42L

        private val DISPLAY_SETTINGS = DisplaySettings(
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            isSortOrderChangeable = true,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            isDisplayOrderChangeable = true,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            isPhoneticNameDisplayChangeable = true,
        )
    }
}
