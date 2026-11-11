package com.android.contacts.data.contacts.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract.Directory
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface DirectoriesRepository {
    fun observeRemote(): Flow<List<DirectoryColumn>>
}

internal class DirectoriesRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DirectoriesRepository {

    override fun observeRemote(): Flow<List<DirectoryColumn>> {
        return observeContentUri(contentResolver, Directory.CONTENT_URI)
            .map { queryRemoteDirectories() }
            .flowOn(ioDispatcher)
    }

    private fun queryRemoteDirectories(): List<DirectoryColumn> {
        @Suppress("TooGenericExceptionCaught")
        return try {
            contentResolver.query(
                Directory.CONTENT_URI,
                PROJECTION,
                SELECTION,
                null,
                Directory._ID,
            )?.use { it.toDirectories() }
                .orEmpty()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Failed to query directories", e)
            emptyList()
        }
    }

    private fun Cursor.toDirectories(): List<DirectoryColumn> {
        return buildList {
            while (moveToNext()) {
                val id = getLong(ID)
                if (!Directory.isRemoteDirectoryId(id)) continue
                add(
                    DirectoryColumn(
                        id = id,
                        displayName = getString(DISPLAY_NAME)?.takeIf { it.isNotBlank() },
                        packageName = getString(PACKAGE_NAME)?.takeIf { it.isNotEmpty() },
                        typeResourceId = getInt(TYPE_RESOURCE_ID).takeIf { it != 0 },
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "DirectoriesRepository"

        @VisibleForTesting
        val PROJECTION = arrayOf(
            Directory._ID,
            Directory.PACKAGE_NAME,
            Directory.TYPE_RESOURCE_ID,
            Directory.DISPLAY_NAME,
        )
        private const val ID = 0
        private const val PACKAGE_NAME = 1
        private const val TYPE_RESOURCE_ID = 2
        private const val DISPLAY_NAME = 3

        @VisibleForTesting
        const val SELECTION = Directory.SHORTCUT_SUPPORT + "=" + Directory.SHORTCUT_SUPPORT_FULL
    }
}
