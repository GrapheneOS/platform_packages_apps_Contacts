package com.android.contacts.data.profile.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.OperationCanceledException
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.DisplayNameSources
import android.provider.ContactsContract.Profile
import android.util.Log
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.preference.ContactsPreferences
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface ProfileRepository {
    fun observeProfile(): Flow<ProfileData>
}

internal class ProfileRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val contactsPreferences: ContactsPreferences,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override fun observeProfile(): Flow<ProfileData> {
        return observeUri(Profile.CONTENT_URI)
            .map { loadProfile() }
            .flowOn(ioDispatcher)
    }

    private fun observeUri(uri: Uri): Flow<Unit> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            contentResolver.registerContentObserver(uri, true, observer)
            trySend(Unit)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    private fun loadProfile(): ProfileData {
        return try {
            queryProfile()
        } catch (e: SecurityException) {
            emptyProfile(e)
        } catch (e: SQLiteException) {
            emptyProfile(e)
        } catch (e: OperationCanceledException) {
            emptyProfile(e)
        }
    }

    private fun emptyProfile(cause: Exception): ProfileData {
        Log.w(TAG, "Could not load the user profile", cause)

        return ProfileData()
    }

    private fun queryProfile(): ProfileData {
        val cursor = contentResolver.query(
            Profile.CONTENT_URI,
            profileProjection(),
            null,
            null,
            null,
        ) ?: return ProfileData()

        return cursor.use(::toProfileData)
    }

    private fun profileProjection(): Array<String> {
        return when (contactsPreferences.displayOrder) {
            ContactsPreferences.DISPLAY_ORDER_PRIMARY -> PROFILE_PROJECTION_PRIMARY
            else -> PROFILE_PROJECTION_ALTERNATIVE
        }
    }

    private fun toProfileData(cursor: Cursor): ProfileData {
        if (!cursor.moveToFirst()) {
            return ProfileData()
        }

        return ProfileData(
            hasProfile = cursor.getInt(IS_USER_PROFILE_INDEX) == 1,
            contactId = cursor.getLong(CONTACT_ID_INDEX),
            displayName = cursor.getString(DISPLAY_NAME_INDEX),
            isDisplayNameFromPhoneNumber =
                cursor.getInt(DISPLAY_NAME_SOURCE_INDEX) == DisplayNameSources.PHONE,
        )
    }

    private companion object {
        const val TAG = "ProfileRepository"

        val PROFILE_PROJECTION_PRIMARY = arrayOf(
            Contacts._ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.IS_USER_PROFILE,
            Contacts.DISPLAY_NAME_SOURCE,
        )

        val PROFILE_PROJECTION_ALTERNATIVE = arrayOf(
            Contacts._ID,
            Contacts.DISPLAY_NAME_ALTERNATIVE,
            Contacts.IS_USER_PROFILE,
            Contacts.DISPLAY_NAME_SOURCE,
        )

        const val CONTACT_ID_INDEX = 0
        const val DISPLAY_NAME_INDEX = 1
        const val IS_USER_PROFILE_INDEX = 2
        const val DISPLAY_NAME_SOURCE_INDEX = 3
    }
}
