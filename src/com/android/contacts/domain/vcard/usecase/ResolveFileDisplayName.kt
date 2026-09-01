package com.android.contacts.domain.vcard.usecase

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import javax.inject.Inject

internal fun interface ResolveFileDisplayName {
    operator fun invoke(fileUri: Uri): String
}

internal class ResolveFileDisplayNameImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : ResolveFileDisplayName {
    override fun invoke(fileUri: Uri): String {
        return resolveDisplayName(fileUri)
            ?: fileUri.lastPathSegment
            ?: fileUri.toString()
    }

    private fun resolveDisplayName(fileUri: Uri): String? {
        return contentResolver.query(
            fileUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.count <= 0 || !cursor.moveToFirst()) {
                return@use null
            }
            if (cursor.count > 1) {
                Log.w(TAG, "Unexpected multiple rows: ${cursor.count}")
            }

            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                return@use cursor.getString(index).ifBlank { null }
            }

            null
        }
    }

    private companion object {
        private const val TAG = "ResolveFileDisplayName"
    }
}
