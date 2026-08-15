package com.android.contacts.data.contactdetails.repository

import android.accounts.Account
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.net.Uri
import android.provider.Contacts as LegacyContacts
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.Directory
import android.provider.ContactsContract.DisplayNameSources
import android.provider.ContactsContract.RawContacts
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contactdetails.mapper.ContactDetailsMapper
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.data.contactdetails.source.ContactLoaderSource
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.di.core.MainDispatcher
import com.android.contacts.model.Contact
import com.android.contacts.model.ContactLoader
import com.android.contacts.quickcontact.InvisibleContactUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface ContactDetailsRepository {

    fun observeContactDetails(
        lookupUri: Uri,
        excludedMimeTypes: Set<String>,
    ): Flow<ContactDetailsResult>

    fun getDirectoryContactPrefill(): DirectoryContactPrefill?

    fun addLoadedContactToDefaultGroup(callbackActivity: Class<out Activity>)

    fun cacheLoadedContact()
}

internal class ContactDetailsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contactLoaderSource: ContactLoaderSource,
    private val contactDetailsMapper: ContactDetailsMapper,
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ContactDetailsRepository {

    @Volatile
    private var contactLoader: ContactLoader? = null

    @Volatile
    private var loadedContact: Contact? = null

    override fun observeContactDetails(
        lookupUri: Uri,
        excludedMimeTypes: Set<String>,
    ): Flow<ContactDetailsResult> {
        return flow {
            when (val uri = toContactLookupUri(lookupUri)) {
                null -> emit(ContactDetailsResult.NotFound)

                else -> emitAll(
                    observeContact(uri).map { contact ->
                        toResult(contact, excludedMimeTypes)
                    },
                )
            }
        }.flowOn(ioDispatcher)
    }

    override fun getDirectoryContactPrefill(): DirectoryContactPrefill? {
        val contact = loadedContact ?: return null
        val values = contact.contentValues.toMutableList()

        if (contact.displayNameSource == DisplayNameSources.ORGANIZATION) {
            values.add(organizationValues(contact.displayName))
        }

        val sameAccountOnly =
            contact.directoryExportSupport == Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY

        return DirectoryContactPrefill(
            name = prefillName(contact),
            values = values,
            account = directoryAccount(contact).takeIf { sameAccountOnly },
            dataSet = contact.rawContacts.firstOrNull()?.dataSet.takeIf { sameAccountOnly },
        )
    }

    override fun addLoadedContactToDefaultGroup(callbackActivity: Class<out Activity>) {
        val contact = loadedContact ?: return
        val deltas = contact.createRawContactDeltaList()

        if (!InvisibleContactUtil.markAddToDefaultGroup(contact, deltas, context)) {
            return
        }

        val intent = ContactSaveService.createSaveContactIntent(
            context,
            deltas,
            NO_SAVE_MODE_EXTRA_KEY,
            SAVE_MODE_CLOSE,
            false,
            callbackActivity,
            Intent.ACTION_VIEW,
            null,
            null,
            null,
        )

        ContactSaveService.startService(context, intent)
    }

    override fun cacheLoadedContact() {
        contactLoader?.cacheResult()
    }

    private fun observeContact(lookupUri: Uri): Flow<Contact> {
        return callbackFlow {
            val loader = contactLoaderSource.create(lookupUri)
            val listener = Loader.OnLoadCompleteListener { _, contact ->
                retainLoadedContact(contact)
                trySend(contact)
            }

            contactLoader = loader
            startLoading(loader, listener)

            awaitClose {
                release(loader, listener)
                contactLoader = null
                loadedContact = null
            }
        }.flowOn(mainDispatcher)
    }

    private fun retainLoadedContact(contact: Contact) {
        if (!contact.isLoaded) {
            return
        }

        loadedContact = contact
    }

    private fun startLoading(
        loader: ContactLoader,
        listener: Loader.OnLoadCompleteListener<Contact>,
    ) {
        loader.registerListener(LOADER_ID, listener)
        loader.startLoading()
    }

    private fun release(
        loader: ContactLoader,
        listener: Loader.OnLoadCompleteListener<Contact>,
    ) {
        loader.stopLoading()
        loader.unregisterListener(listener)
        loader.reset()
    }

    private fun toContactLookupUri(lookupUri: Uri): Uri? {
        if (lookupUri.authority != LegacyContacts.AUTHORITY) {
            return lookupUri
        }

        val rawContactId = ContentUris.parseId(lookupUri)
        val rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId)

        return RawContacts.getContactLookupUri(contentResolver, rawContactUri)
    }

    private fun toResult(
        contact: Contact,
        excludedMimeTypes: Set<String>,
    ): ContactDetailsResult {
        return when {
            contact.isError -> ContactDetailsResult.Error
            contact.isNotFound -> ContactDetailsResult.NotFound

            else -> ContactDetailsResult.Loaded(
                contactDetailsMapper.map(contact, excludedMimeTypes),
            )
        }
    }

    private fun prefillName(contact: Contact): String? {
        return when {
            contact.displayNameSource >= DisplayNameSources.NICKNAME -> contact.displayName
            else -> null
        }
    }

    private fun directoryAccount(contact: Contact): Account {
        return Account(contact.directoryAccountName, contact.directoryAccountType)
    }

    private fun organizationValues(displayName: String?): ContentValues {
        return ContentValues().apply {
            put(Organization.COMPANY, displayName)
            put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
        }
    }

    private companion object {
        const val LOADER_ID = 1
        const val NO_SAVE_MODE_EXTRA_KEY = ""
        const val SAVE_MODE_CLOSE = 0
    }
}
