package com.android.contacts.domain.vcard.usecase

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.util.SaveUriToFile
import com.android.contacts.domain.vcard.model.ImportVCardSource as Source
import com.android.contacts.vcard.VCardService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface BuildVCardSource {
    suspend operator fun invoke(uri: Uri): Source?
}

internal class BuildVCardSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val resolveFileDisplayName: ResolveFileDisplayName,
    private val saveUriToFile: SaveUriToFile,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : BuildVCardSource {
    override suspend fun invoke(uri: Uri): Source? {
        return withContext(coroutineDispatcher) {
            build(uri)
        }
    }

    private suspend fun build(sourceUri: Uri): Source? {
        val filename = getLocalFilename()
        if (!saveUriToFile(sourceUri = sourceUri, destinationFilename = filename)) {
            return null
        }
        return Source(
            uri = context.getFileStreamPath(filename).toURI().toString().toUri(),
            name = resolveFileDisplayName(sourceUri),
        )
    }

    // Cache files are deleted by the VCardService
    private fun getLocalFilename(): String {
        for (cacheIndex in 0 until Int.MAX_VALUE) {
            val filename = VCardService.CACHE_FILE_PREFIX + cacheIndex + ".vcf"
            val file = context.getFileStreamPath(filename)
            if (!file.exists()) {
                return filename
            }
        }

        throw IllegalStateException("Exceeded cache limit")
    }
}
