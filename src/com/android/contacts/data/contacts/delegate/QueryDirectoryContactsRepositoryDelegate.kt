package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.delegate.util.getContactPhoto
import com.android.contacts.data.contacts.model.DirectoryContactColumn
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal interface QueryDirectoryContactsRepositoryDelegate {
    fun queryDirectoryContacts(
        directoryId: Long,
        searchQuery: String,
    ): Flow<List<DirectoryContactColumn>>
}

internal class QueryDirectoryContactsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : QueryDirectoryContactsRepositoryDelegate {

    override fun queryDirectoryContacts(
        directoryId: Long,
        searchQuery: String,
    ): Flow<List<DirectoryContactColumn>> {
        return flow { emit(query(directoryId, searchQuery)) }
            .flowOn(ioDispatcher)
    }

    private fun query(
        directoryId: Long,
        searchQuery: String,
    ): List<DirectoryContactColumn> {
        @Suppress("TooGenericExceptionCaught")
        return try {
            contentResolver.query(
                buildUri(directoryId, searchQuery),
                PROJECTION,
                null,
                null,
                null,
            )?.use { it.toDirectoryContactColumns(directoryId) }
                .orEmpty()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Failed to query directory $directoryId", e)
            emptyList()
        }
    }

    private fun buildUri(directoryId: Long, searchQuery: String): Uri {
        return Contacts.CONTENT_FILTER_URI.buildUpon()
            .appendPath(searchQuery)
            .appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, directoryId.toString())
            .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, RESULT_LIMIT.toString())
            .build()
    }

    private fun Cursor.toDirectoryContactColumns(directoryId: Long): List<DirectoryContactColumn> {
        return buildList {
            while (moveToNext()) {
                val lookupKey = getString(LOOKUP_KEY)
                    ?.takeIf { it.isNotBlank() }
                    ?: continue
                add(
                    DirectoryContactColumn(
                        directoryId = directoryId,
                        id = getLong(ID),
                        lookupKey = lookupKey,
                        displayNamePrimary = getString(DISPLAY_NAME_PRIMARY),
                        displayNameAlternative = getString(DISPLAY_NAME_ALTERNATIVE),
                        photo = getContactPhoto(PHOTO_THUMBNAIL_URI),
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "QueryDirectoryContacts"

        @VisibleForTesting
        const val RESULT_LIMIT = 20

        @VisibleForTesting
        val PROJECTION = arrayOf(
            Contacts._ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.DISPLAY_NAME_ALTERNATIVE,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.LOOKUP_KEY,
        )
        private const val ID = 0
        private const val DISPLAY_NAME_PRIMARY = 1
        private const val DISPLAY_NAME_ALTERNATIVE = 2
        private const val PHOTO_THUMBNAIL_URI = 3
        private const val LOOKUP_KEY = 4
    }
}
