package com.android.contacts.domain.vcard.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.android.contacts.R
import com.android.contacts.di.core.CacheDir
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.vcard.usecase.CreateTempExportFile.Companion.FILE_PREFIX
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface CreateTempExportFile {
    suspend operator fun invoke(): Uri?

    companion object {
        const val FILE_PREFIX = "vcards_"
    }
}

internal class CreateTempExportFileImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:CacheDir private val cacheDir: File,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : CreateTempExportFile {

    override suspend operator fun invoke(): Uri? {
        return withContext(coroutineDispatcher) {
            val file = File(cacheDir, getFileName())

            try {
                file.createNewFile()
            } catch (e: IOException) {
                Log.w(TAG, "Failed to create .vcf file", e)
                return@withContext null
            }

            return@withContext try {
                FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.contacts_file_provider_authority),
                    file,
                )
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Failed to get uri for .vcf file", e)
                null
            }
        }
    }

    private fun getFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val currentDateString = dateFormat.format(Date()).toString()
        return "$FILE_PREFIX$currentDateString.vcf"
    }

    companion object {
        private const val TAG = "CreateTemporaryFile"
    }
}
