package com.android.contacts.domain.contacts.usecase

import android.content.ContentUris
import android.provider.ContactsContract.Contacts
import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.LoadedContact
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.model.Contact
import com.android.contacts.tests.factory.contactDetails
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class GetContactTest {

    private val contactDetailsRepository = mockk<ContactDetailsRepository>(relaxed = true)

    private val subject: GetContact = GetContactImpl(
        contactDetailsRepository = contactDetailsRepository,
    )

    @Test
    fun invoke_observesContactUriWithoutExcludedMimeTypes() = runTest {
        every { contactDetailsRepository.observeContactDetails(any(), any()) } returns emptyFlow()

        subject(CONTACT_ID).test { awaitComplete() }

        val expectedUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, CONTACT_ID)
        verify {
            contactDetailsRepository.observeContactDetails(expectedUri, emptySet())
        }
    }

    @Test
    fun invoke_whenLoaded_emitsContact() = runTest {
        val contact = mockk<Contact>()
        every {
            contactDetailsRepository.observeContactDetails(any(), any())
        } returns flowOf(loaded(contact))

        assertSame(contact, subject(CONTACT_ID).first())
    }

    @Test
    fun invoke_skipsNotFoundAndErrorResults() = runTest {
        val results = MutableSharedFlow<ContactDetailsResult>()
        every { contactDetailsRepository.observeContactDetails(any(), any()) } returns results
        val first = mockk<Contact>()
        val second = mockk<Contact>()

        subject(CONTACT_ID).test {
            results.emit(ContactDetailsResult.NotFound)
            results.emit(loaded(first))
            results.emit(ContactDetailsResult.Error)
            results.emit(loaded(second))

            assertSame(first, awaitItem())
            assertSame(second, awaitItem())
            expectNoEvents()
        }
    }

    private fun loaded(contact: Contact): ContactDetailsResult.Loaded {
        return ContactDetailsResult.Loaded(
            details = contactDetails(),
            source = LoadedContact(contact, mockk(relaxed = true)),
        )
    }

    companion object {
        private const val CONTACT_ID = 42L
    }
}
