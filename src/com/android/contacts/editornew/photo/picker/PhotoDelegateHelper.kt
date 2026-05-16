package com.android.contacts.editornew.photo.picker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.ContactsContract
import com.android.contacts.util.ContactPhotoUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val DEFAULT_PHOTO_DIMENSION = 720

internal class PhotoDelegateHelper
@Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) {
    fun tmpPhotoUri(): Uri = ContactPhotoUtils.generateTempImageUri(context)

    fun tmpCroppedPhotoUri(): Uri = ContactPhotoUtils.generateTempCroppedImageUri(context)

    fun deleteTemporaryPhoto(uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    fun intentHandlerOrNull(intent: Intent): ResolveInfo? {
        return context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY,
        )
            .takeIf { it.isNotEmpty() }
            ?.get(0)
    }

    fun uriToWriteableTmpImageUri(uri: Uri): Uri? {
        val tmpUri = tmpPhotoUri()
        return try {
            val success = ContactPhotoUtils.savePhotoFromUriToUri(context, uri, tmpUri, false)
            if (success) tmpUri else null
        } catch (_: SecurityException) {
            null
        }
    }

    fun photoPickDimension(): Int {
        return queryPhotoDimension()
            ?.takeIf { it != 0 }
            ?: DEFAULT_PHOTO_DIMENSION
    }

    private fun queryPhotoDimension(): Int? {
        return context.contentResolver.query(
            ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
            arrayOf(ContactsContract.DisplayPhoto.DISPLAY_MAX_DIM),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                null
            }
        }
    }
}
