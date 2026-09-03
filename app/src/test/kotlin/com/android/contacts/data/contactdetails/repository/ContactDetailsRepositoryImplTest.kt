package com.android.contacts.data.contactdetails.repository

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.Directory
import android.provider.ContactsContract.DisplayNameSources
import android.provider.ContactsContract.RawContacts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contactdetails.mapper.ContactDetailsMapper
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.data.contactdetails.source.ContactLoaderSource
import com.android.contacts.model.Contact
import com.android.contacts.model.ContactLoader
import com.android.contacts.model.RawContact
import com.android.contacts.quickcontact.InvisibleContactUtil
import com.android.contacts.tests.factory.contactDetails
import com.google.common.collect.ImmutableList
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsRepositoryImplTest {

    private val contactLoaderSource = mockk<ContactLoaderSource>()
    private val contactDetailsMapper = mockk<ContactDetailsMapper>()
    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val contactLoader = mockk<ContactLoader>(relaxed = true)
    private val listenerSlot = slot<Loader.OnLoadCompleteListener<Contact>>()
    private val serviceIntentSlot = slot<Intent>()
    private val context = spyk<Context>(RuntimeEnvironment.getApplication())

    private val repository = ContactDetailsRepositoryImpl(
        context = context,
        contactLoaderSource = contactLoaderSource,
        contactDetailsMapper = contactDetailsMapper,
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
        mainDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        every { contactLoaderSource.create(any()) } returns contactLoader
        every { contactLoader.registerListener(any(), capture(listenerSlot)) } just Runs
        every { contactDetailsMapper.map(any(), any()) } returns MAPPED_DETAILS
        every { context.startService(capture(serviceIntentSlot)) } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun observeContactDetails_whenTheLoaderDeliversAContact_emitsLoadedDetails() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            val contact = loadedContact()
            deliver(contact)

            val loaded = awaitLoaded()

            assertEquals(MAPPED_DETAILS, loaded.details)
            assertSame(contact, loaded.source.contact)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_passesTheExcludedMimeTypesToTheMapper() = runTest {
        val contact = loadedContact()
        val excluded = setOf(Organization.CONTENT_ITEM_TYPE)

        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = excluded).test {
            deliver(contact)

            awaitItem()
            verify { contactDetailsMapper.map(contact, excludedMimeTypes = excluded) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_whenTheContactIsNotFound_emitsNotFound() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(contact(isNotFound = true))

            assertEquals(ContactDetailsResult.NotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_whenTheContactFailsToLoad_emitsError() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(contact(isError = true))

            assertEquals(ContactDetailsResult.Error, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_whenTheLoaderDeliversAgain_emitsAgain() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(loadedContact())
            awaitItem()

            deliver(loadedContact())

            assertEquals(MAPPED_DETAILS, awaitLoaded().details)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_startsTheLoaderForTheLookupUri() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            verify { contactLoaderSource.create(LOOKUP_URI) }
            verify { contactLoader.startLoading() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeContactDetails_withALegacyAuthorityUri_startsTheLoaderForTheContactLookupUri() =
        runTest {
            givenLookupRow(contactId = 7L, lookupKey = "lookup-key")

            repository.observeContactDetails(LEGACY_URI, excludedMimeTypes = emptySet()).test {
                verify { contactLoaderSource.create(Contacts.getLookupUri(7L, "lookup-key")) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeContactDetails_withALegacyAuthorityUriThatNoLongerResolves_emitsNotFound() =
        runTest {
            givenNoLookupRow()

            repository.observeContactDetails(LEGACY_URI, excludedMimeTypes = emptySet()).test {
                assertEquals(ContactDetailsResult.NotFound, awaitItem())
                verify(exactly = 0) { contactLoaderSource.create(any()) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeContactDetails_whenCollectionStops_releasesTheLoader() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            cancelAndIgnoreRemainingEvents()
        }

        verify { contactLoader.stopLoading() }
        verify { contactLoader.unregisterListener(any()) }
        verify { contactLoader.reset() }
    }

    @Test
    fun cacheContact_cachesTheLoaderResult() = runTest {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(loadedContact())

            repository.cacheContact(awaitLoaded().source)

            verify { contactLoader.cacheResult() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDirectoryContactPrefill_withANameSourceAboveNickname_prefillsTheName() = runTest {
        val contact = loadedContact(
            displayName = "Alex Doe",
            displayNameSource = DisplayNameSources.STRUCTURED_NAME,
        )

        withPrefill(contact) { prefill ->
            assertEquals("Alex Doe", prefill.name)
            assertEquals(1, prefill.values.size)
        }
    }

    @Test
    fun getDirectoryContactPrefill_withAnEmailNameSource_doesNotPrefillTheName() = runTest {
        val contact = loadedContact(
            displayName = "alex@example.org",
            displayNameSource = DisplayNameSources.EMAIL,
        )

        withPrefill(contact) { prefill ->
            assertNull(prefill.name)
            assertEquals(1, prefill.values.size)
        }
    }

    @Test
    fun getDirectoryContactPrefill_withAnOrganizationNameSource_addsAnOrganizationRow() = runTest {
        val contact = loadedContact(
            displayName = "Acme",
            displayNameSource = DisplayNameSources.ORGANIZATION,
        )

        withPrefill(contact) { prefill ->
            val organization = prefill.values.last()

            assertNull(prefill.name)
            assertEquals(Organization.CONTENT_ITEM_TYPE, organization.getAsString(Data.MIMETYPE))
            assertEquals("Acme", organization.getAsString(Organization.COMPANY))
        }
    }

    @Test
    fun getDirectoryContactPrefill_withASameAccountOnlyDirectory_returnsTheAccount() = runTest {
        val contact = loadedContact(
            exportSupport = Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY,
            dataSet = "plus",
        )

        withPrefill(contact) { prefill ->
            assertEquals("directory@example.org", prefill.account?.name)
            assertEquals("com.example.directory", prefill.account?.type)
            assertEquals("plus", prefill.dataSet)
        }
    }

    @Test
    fun getDirectoryContactPrefill_withAnyAccountDirectory_returnsNoAccount() = runTest {
        val contact = loadedContact(
            exportSupport = Directory.EXPORT_SUPPORT_ANY_ACCOUNT,
            dataSet = "plus",
        )

        withPrefill(contact) { prefill ->
            assertNull(prefill.account)
            assertNull(prefill.dataSet)
        }
    }

    @Test
    fun addToDefaultGroup_startsTheSaveService() = runTest {
        givenTheContactCanBeAdded(canBeAdded = true)

        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(loadedContact())

            repository.addToDefaultGroup(awaitLoaded().source, Activity::class.java)

            val intent = serviceIntentSlot.captured
            val callbackIntent = intent.getParcelableExtra(
                ContactSaveService.EXTRA_CALLBACK_INTENT,
                Intent::class.java,
            )

            assertEquals(ContactSaveService.ACTION_SAVE_CONTACT, intent.action)
            assertEquals(Activity::class.java.name, callbackIntent?.component?.className)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addToDefaultGroup_withoutADefaultGroup_doesNotStartTheSaveService() = runTest {
        givenTheContactCanBeAdded(canBeAdded = false)

        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(loadedContact())

            repository.addToDefaultGroup(awaitLoaded().source, Activity::class.java)

            verify(exactly = 0) { context.startService(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addToDefaultGroup_addsTheGivenContact() = runTest {
        givenTheContactCanBeAdded(canBeAdded = true)
        val contact = loadedContact()

        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(contact)

            repository.addToDefaultGroup(awaitLoaded().source, Activity::class.java)

            verify { InvisibleContactUtil.markAddToDefaultGroup(contact, any(), any()) }
            assertNotNull(serviceIntentSlot.captured)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun givenTheContactCanBeAdded(canBeAdded: Boolean) {
        mockkStatic(InvisibleContactUtil::class)
        every { InvisibleContactUtil.markAddToDefaultGroup(any(), any(), any()) } returns canBeAdded
    }

    private suspend fun withPrefill(
        contact: Contact,
        assertions: (DirectoryContactPrefill) -> Unit,
    ) {
        repository.observeContactDetails(LOOKUP_URI, excludedMimeTypes = emptySet()).test {
            deliver(contact)

            assertions(repository.getDirectoryContactPrefill(awaitLoaded().source))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun ReceiveTurbine<ContactDetailsResult>.awaitLoaded():
        ContactDetailsResult.Loaded {
        return awaitItem() as ContactDetailsResult.Loaded
    }

    private fun givenLookupRow(
        contactId: Long,
        lookupKey: String,
    ) {
        val cursor = MatrixCursor(arrayOf(RawContacts.CONTACT_ID, Contacts.LOOKUP_KEY))
        cursor.addRow(arrayOf<Any>(contactId, lookupKey))
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor
    }

    private fun givenNoLookupRow() {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null
    }

    private fun deliver(contact: Contact) {
        listenerSlot.captured.onLoadComplete(contactLoader, contact)
    }

    private fun loadedContact(
        displayName: String? = "Alex Doe",
        displayNameSource: Int = DisplayNameSources.STRUCTURED_NAME,
        exportSupport: Int = Directory.EXPORT_SUPPORT_ANY_ACCOUNT,
        dataSet: String? = null,
    ): Contact {
        return contact(
            isLoaded = true,
            displayName = displayName,
            displayNameSource = displayNameSource,
            exportSupport = exportSupport,
            dataSet = dataSet,
        )
    }

    private fun contact(
        isLoaded: Boolean = false,
        isNotFound: Boolean = false,
        isError: Boolean = false,
        displayName: String? = "Alex Doe",
        displayNameSource: Int = DisplayNameSources.STRUCTURED_NAME,
        exportSupport: Int = Directory.EXPORT_SUPPORT_ANY_ACCOUNT,
        dataSet: String? = null,
    ): Contact {
        val rawContact = mockk<RawContact>(relaxed = true)
        every { rawContact.dataSet } returns dataSet

        val contact = mockk<Contact>(relaxed = true)
        every { contact.isLoaded } returns isLoaded
        every { contact.isNotFound } returns isNotFound
        every { contact.isError } returns isError
        every { contact.displayName } returns displayName
        every { contact.displayNameSource } returns displayNameSource
        every { contact.directoryExportSupport } returns exportSupport
        every { contact.directoryAccountName } returns "directory@example.org"
        every { contact.directoryAccountType } returns "com.example.directory"
        every { contact.rawContacts } returns ImmutableList.of(rawContact)
        every { contact.contentValues } returns arrayListOf(ContentValues())
        return contact
    }

    private companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
        val LEGACY_URI: Uri = Uri.parse("content://contacts/people/7")

        val MAPPED_DETAILS = contactDetails(lookupUri = LOOKUP_URI)
    }
}
