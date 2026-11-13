package com.android.contacts.data.photo.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.DisplayPhoto
import android.provider.ContactsContract.RawContacts
import android.util.Log
import com.android.contacts.ContactSaveService
import com.android.contacts.data.photo.delegate.ContactPhotoManipulationRepositoryDelegate
import com.android.contacts.data.photo.delegate.ContactPhotoManipulationRepositoryDelegateImpl
import com.android.contacts.data.photo.model.TempPhotoUris
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.Contact
import com.android.contacts.model.ContactLoader
import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.RawContactDeltaList
import com.android.contacts.model.RawContactModifier
import com.android.contacts.model.ValuesDelta
import com.android.contacts.util.ContactPhotoUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface ContactPhotoRepository : ContactPhotoManipulationRepositoryDelegate {

    suspend fun getMaxPhotoDimension(): Int?

    suspend fun generateTempPhotoUris(): TempPhotoUris

    suspend fun copyPhoto(from: Uri, to: Uri): Boolean

    suspend fun deletePhoto(uri: Uri)

    suspend fun savePhotoToRawContact(
        contactUri: Uri,
        rawContactId: Long,
        thumbnail: ByteArray,
        photoUri: Uri,
    ): Boolean

    suspend fun savePhotoToNewRawContact(
        contactUri: Uri,
        account: AccountModel,
        thumbnail: ByteArray,
        photoUri: Uri,
    ): Boolean
}

@Singleton
internal class ContactPhotoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    private val accountTypeManager: AccountTypeManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val photoManipulationDelegateImpl: ContactPhotoManipulationRepositoryDelegateImpl,
) : ContactPhotoRepository,
    ContactPhotoManipulationRepositoryDelegate by photoManipulationDelegateImpl {

    @Volatile
    private var maxPhotoDimension: Int? = null

    override suspend fun getMaxPhotoDimension(): Int? {
        maxPhotoDimension?.let { return it }
        return withContext(ioDispatcher) {
            queryMaxPhotoDimension()?.also { maxPhotoDimension = it }
        }
    }

    private fun queryMaxPhotoDimension(): Int? {
        return contentResolver.query(
            DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
            arrayOf(DisplayPhoto.DISPLAY_MAX_DIM),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0).takeIf { it > 0 } else null
        }
    }

    override suspend fun generateTempPhotoUris(): TempPhotoUris {
        return withContext(ioDispatcher) {
            TempPhotoUris(
                temp = ContactPhotoUtils.generateTempImageUri(context),
                cropped = ContactPhotoUtils.generateTempCroppedImageUri(context),
            )
        }
    }

    override suspend fun copyPhoto(from: Uri, to: Uri): Boolean {
        return withContext(ioDispatcher) {
            ContactPhotoUtils.savePhotoFromUriToUri(context, from, to, false)
        }
    }

    override suspend fun deletePhoto(uri: Uri) {
        withContext(ioDispatcher) {
            contentResolver.delete(uri, null, null)
        }
    }

    override suspend fun savePhotoToRawContact(
        contactUri: Uri,
        rawContactId: Long,
        thumbnail: ByteArray,
        photoUri: Uri,
    ): Boolean {
        return savePhoto(
            contactUri = contactUri,
            thumbnail = thumbnail,
            photoUri = photoUri,
            selectRawContactDelta = { deltaList ->
                deltaList.getByRawContactId(rawContactId)
            }
        )
    }

    override suspend fun savePhotoToNewRawContact(
        contactUri: Uri,
        account: AccountModel,
        thumbnail: ByteArray,
        photoUri: Uri,
    ): Boolean {
        return savePhoto(
            contactUri = contactUri,
            thumbnail = thumbnail,
            photoUri = photoUri,
            selectRawContactDelta = { deltaList ->
                buildNewRawContactDelta(account)
                    .also(deltaList::add)
            }
        )
    }

    private suspend fun savePhoto(
        contactUri: Uri,
        thumbnail: ByteArray,
        photoUri: Uri,
        selectRawContactDelta: (RawContactDeltaList) -> RawContactDelta?,
    ): Boolean {
        return withContext(ioDispatcher) {
            val contact = loadContact(contactUri)
                ?: run {
                    Log.w(TAG, "Could not load contact")
                    return@withContext false
                }

            val deltaList = contact.createRawContactDeltaList()
            val rawContactDelta = selectRawContactDelta(deltaList)
                ?: run {
                    Log.w(TAG, "Raw contact not found")
                    return@withContext false
                }

            val accountType = rawContactDelta.getAccountType(accountTypeManager)
            val values = RawContactModifier.ensureKindExists(
                rawContactDelta,
                accountType,
                Photo.CONTENT_ITEM_TYPE,
            ) ?: run {
                Log.w(TAG, "Cannot attach photo to this account type")
                return@withContext false
            }

            // Add compressed bitmap to entity-delta. This allows us to save to a new contact;
            // otherwise the entity-delta-list would be empty, and the ContactSaveService would
            // not create the new contact, and the full-res photo would fail to be saved to the
            // non-existent contact.
            values.photo = thumbnail

            val intent = ContactSaveService.createSaveContactIntent(
                context,
                deltaList,
                "",
                0,
                contact.isUserProfile,
                null,
                null,
                rawContactDelta.rawContactId ?: -1L,
                photoUri,
            )
            startService(intent)
        }
    }

    private fun loadContact(contactUri: Uri): Contact? =
        ContactLoader(context, contactUri, POST_VIEW_NOTIFICATION)
            .loadInBackground()
            ?.takeIf { it.isLoaded && it.rawContacts != null }

    private fun startService(intent: Intent): Boolean {
        return try {
            context.startService(intent)
            true
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Could not start ContactSaveService", e)
            false
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not start ContactSaveService", e)
            false
        }
    }

    private fun buildNewRawContactDelta(account: AccountModel): RawContactDelta {
        val contentValues = ContentValues().apply {
            put(RawContacts.ACCOUNT_TYPE, account.type)
            put(RawContacts.ACCOUNT_NAME, account.name)
            put(RawContacts.DATA_SET, account.dataSet)
        }
        return RawContactDelta(ValuesDelta.fromAfter(contentValues))
    }

    private companion object {
        const val TAG = "ContactPhotoRepository"
        const val POST_VIEW_NOTIFICATION = false
    }
}
