package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.android.contacts.data.contacts.model.RawContact
import com.android.contacts.data.contacts.model.RawContactsMetadata
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface LoadRawContactsRepositoryDelegate {
    fun loadRawContacts(contactUri: Uri): Flow<RawContactsMetadata?>
}

internal class LoadRawContactsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadRawContactsRepositoryDelegate {
    override fun loadRawContacts(contactUri: Uri): Flow<RawContactsMetadata?> {
        ensureIsContactUri(contactUri)
        return observeContentUri(contentResolver, contactUri)
            .map { load(contactUri) }
            .flowOn(coroutineDispatcher)
    }

    private fun ensureIsContactUri(uri: Uri) {
        require(
            uri.toString().startsWith(ContactsContract.Contacts.CONTENT_URI.toString()) ||
                uri.toString().startsWith(ContactsContract.Profile.CONTENT_URI.toString()),
        ) { "Invalid contact Uri: $uri" }
    }

    private fun load(contactUri: Uri): RawContactsMetadata? {
        val (contactId, isUserProfile) = queryProfile(contactUri) ?: return null
        return queryRawContacts(contactId, isUserProfile)
            ?.let { rawContacts ->
                val rawContactsWithPhotos = addPhotosToRawContacts(rawContacts, isUserProfile)
                RawContactsMetadata(
                    contactId = contactId,
                    isUserProfile = isUserProfile,
                    rawContacts = rawContactsWithPhotos,
                )
            }
    }

    private fun queryProfile(contactUri: Uri): Pair<Long, Boolean>? {
        return contentResolver.query(
            contactUri,
            PROFILE_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val contactId = cursor.getLong(CONTACT_ID)
            val isUserProfile = cursor.getInt(IS_USER_PROFILE) == 1
            contactId to isUserProfile
        }
    }

    private fun queryRawContacts(
        contactId: Long,
        isUserProfile: Boolean,
    ): List<RawContact>? {
        val rawContactUri = when {
            isUserProfile -> ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI
            else -> ContactsContract.RawContacts.CONTENT_URI
        }

        return contentResolver.query(
            rawContactUri,
            RAW_CONTACT_PROJECTION,
            RAW_CONTACT_SELECTION,
            arrayOf(contactId.toString()),
            null,
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(buildRawContact(cursor))
                }
            }.takeIf { it.isNotEmpty() }
        }
    }

    private fun buildRawContact(cursor: Cursor): RawContact {
        return RawContact(
            id = cursor.getLong(RAW_CONTACT_ID),
            displayName = cursor.getString(DISPLAY_NAME_PRIMARY),
            displayNameAlt = cursor.getString(DISPLAY_NAME_ALTERNATIVE),
            accountName = cursor.getString(ACCOUNT_NAME),
            accountType = cursor.getString(ACCOUNT_TYPE),
            accountDataSet = cursor.getString(DATA_SET),
            photoUri = null,
        )
    }

    private fun addPhotosToRawContacts(
        rawContacts: List<RawContact>,
        isUserProfile: Boolean,
    ): List<RawContact> {
        val dataUri = when {
            isUserProfile -> Uri.withAppendedPath(
                ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Data.CONTENT_URI.path,
            )
            else -> ContactsContract.Data.CONTENT_URI
        }
        val photoSelection = PHOTO_SELECTION_PREFIX +
            rawContacts.joinToString(",") { it.id.toString() } +
            PHOTO_SELECTION_SUFFIX
        val rawContactsMap = rawContacts.associateBy { it.id }.toMutableMap()

        contentResolver.query(
            dataUri,
            PHOTO_PROJECTION,
            photoSelection,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val rawContactId = cursor.getLong(PHOTO_RAW_CONTACT_ID)
                val photoUri = cursor.getString(PHOTO_THUMBNAIL_URI)
                val rawContact = rawContactsMap[rawContactId] ?: continue
                rawContactsMap[rawContactId] = rawContact.copy(photoUri = photoUri)
            }
        }

        return rawContactsMap.values.toList()
    }

    companion object {
        private val PROFILE_PROJECTION = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.IS_USER_PROFILE,
        )
        private const val CONTACT_ID = 0
        private const val IS_USER_PROFILE = 1

        private val RAW_CONTACT_PROJECTION = arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.DATA_SET,
        )

        private const val RAW_CONTACT_SELECTION =
            ContactsContract.RawContacts.CONTACT_ID + "=?"

        private const val RAW_CONTACT_ID = 0
        private const val DISPLAY_NAME_PRIMARY = 1
        private const val DISPLAY_NAME_ALTERNATIVE = 2
        private const val ACCOUNT_NAME = 3
        private const val ACCOUNT_TYPE = 4
        private const val DATA_SET = 5

        private const val PHOTO_SELECTION_PREFIX =
            ContactsContract.Data.RAW_CONTACT_ID + " IN ("
        private const val PHOTO_SELECTION_SUFFIX =
            ") AND ${ContactsContract.Data.MIMETYPE}=\"${ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE}\""

        private val PHOTO_PROJECTION = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
        )
        private const val PHOTO_RAW_CONTACT_ID = 0
        private const val PHOTO_THUMBNAIL_URI = 1
    }
}
