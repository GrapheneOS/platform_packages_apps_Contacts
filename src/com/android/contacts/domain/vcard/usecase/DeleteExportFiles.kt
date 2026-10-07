package com.android.contacts.domain.vcard.usecase

import com.android.contacts.di.core.CacheDir
import com.android.contacts.di.core.IoDispatcher
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Delete the files (that are untouched for more than 1 day) in the cache directory.
 * We cannot rely on VCardService to delete export files because it will delete export files
 * right after finishing writing so no files could be shared. Therefore, our approach to
 * deleting export files is:
 * 1. put export files in cache directory so that Android may delete them;
 * 2. manually delete the files that are older than 1 day when service is connected.
 */
internal fun interface DeleteExportFiles {
    suspend operator fun invoke()
}

internal class DeleteExportFilesImpl @Inject constructor(
    @param:CacheDir private val cacheDir: File,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : DeleteExportFiles {

    override suspend operator fun invoke() {
        withContext(coroutineDispatcher) {
            cacheDir.listFiles()
                .orEmpty()
                .forEach { file ->
                    val ageInMillis = System.currentTimeMillis() - file.lastModified()
                    if (file.getName().startsWith(CreateTempExportFile.FILE_PREFIX) &&
                        ageInMillis > AGE_THRESHOLD.inWholeMilliseconds
                    ) {
                        file.delete()
                    }
                }
        }
    }

    private companion object {
        val AGE_THRESHOLD = 1.days
    }
}
