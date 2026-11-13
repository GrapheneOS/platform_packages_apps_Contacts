package com.android.contacts.data.photo.delegate

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import com.android.contacts.ContactsUtils
import com.android.contacts.data.photo.model.SquareCrop
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.util.ContactPhotoUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface ContactPhotoManipulationRepositoryDelegate {

    suspend fun buildThumbnail(photoUri: Uri): ByteArray?

    suspend fun cropToSquare(source: Uri, output: Uri, size: Int): Boolean
}

internal class ContactPhotoManipulationRepositoryDelegateImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactPhotoManipulationRepositoryDelegate {

    override suspend fun buildThumbnail(photoUri: Uri): ByteArray? {
        return withContext(ioDispatcher) {
            val size = ContactsUtils.getThumbnailSize(context)
            decodeSquare(photoUri, size, upscale = true)
                ?.let(ContactPhotoUtils::compressBitmap)
        }
    }

    override suspend fun cropToSquare(source: Uri, output: Uri, size: Int): Boolean {
        return withContext(ioDispatcher) {
            val bitmap = decodeSquare(source, size) ?: return@withContext false
            @Suppress("TooGenericExceptionCaught")
            try {
                contentResolver.openOutputStream(output, "wt")?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                } ?: false
            } catch (e: Exception) {
                Log.w(TAG, "Could not write cropped photo", e)
                false
            }
        }
    }

    private fun decodeSquare(uri: Uri, size: Int, upscale: Boolean = false): Bitmap? {
        @Suppress("TooGenericExceptionCaught")
        return try {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val crop = SquareCrop.of(info.size.width, info.size.height, size, upscale)
                decoder.setTargetSize(crop.targetWidth, crop.targetHeight)
                decoder.crop = crop.crop
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not decode photo", e)
            null
        }
    }

    private companion object {
        const val TAG = "PhotoManipulation"
        const val JPEG_QUALITY = 90
    }
}
