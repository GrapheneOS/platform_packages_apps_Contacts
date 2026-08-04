package com.android.contacts.domain.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contactsbind.FeedbackHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface SaveUriToFile {
    suspend operator fun invoke(sourceUri: Uri, destinationFilename: String): Boolean
}

internal class SaveUriToFileImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : SaveUriToFile {
    override suspend fun invoke(sourceUri: Uri, destinationFilename: String): Boolean {
        return withContext(coroutineDispatcher) {
            save(sourceUri, destinationFilename)
        }
    }

    private fun save(sourceUri: Uri, destinationFilename: String): Boolean {
        return try {
            contentResolver.openInputStream(sourceUri)?.use { input ->
                context.openFileOutput(destinationFilename, Context.MODE_PRIVATE)?.use { output ->
                    input.copyTo(output)
                    true
                }
                    ?: run {
                        Log.i(TAG, "Could not open file output for $destinationFilename")
                        false
                    }
            }
                ?: run {
                    Log.i(TAG, "Could not open input stream for $sourceUri")
                    false
                }
        } catch (e: IOException) {
            FeedbackHelper.sendFeedback(context, TAG, "Failed to copy vcard to local file", e)
            false
        } catch (e: SecurityException) {
            FeedbackHelper.sendFeedback(context, TAG, "Failed to copy vcard to local file", e)
            false
        }
    }

    private companion object {
        private const val TAG = "ResolveFileDisplayName"
    }
}
