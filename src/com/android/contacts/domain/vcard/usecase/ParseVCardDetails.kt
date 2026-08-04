package com.android.contacts.domain.vcard.usecase

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.vcard.model.VCardDetails
import com.android.contacts.domain.vcard.model.VCardVersion
import com.android.vcard.VCardEntryCounter
import com.android.vcard.VCardParser
import com.android.vcard.VCardParser_V21
import com.android.vcard.VCardParser_V30
import com.android.vcard.VCardParser_V40
import com.android.vcard.VCardSourceDetector
import com.android.vcard.exception.VCardException
import com.android.vcard.exception.VCardNestedException
import com.android.vcard.exception.VCardVersionException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface ParseVCardDetails {
    @Throws(VCardException::class, IOException::class, OutOfMemoryError::class)
    suspend operator fun invoke(uri: Uri): VCardDetails?
}

internal class ParseVCardDetailsImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : ParseVCardDetails {
    override suspend fun invoke(uri: Uri): VCardDetails {
        return withContext(coroutineDispatcher) {
            buildParsers()
                .firstNotNullOfOrNull { (version, parser) -> getDetails(version, parser, uri) }
                ?: throw VCardException("vCard with unspported version.")
        }
    }

    private fun buildParsers(): Map<VCardVersion, VCardParser> {
        return mapOf(
            VCardVersion.V21 to VCardParser_V21(),
            VCardVersion.V30 to VCardParser_V30(),
            VCardVersion.V40 to VCardParser_V40(),
        )
    }

    private fun getDetails(
        version: VCardVersion,
        parser: VCardParser,
        uri: Uri,
    ): VCardDetails? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val counter = VCardEntryCounter()
                val detector = VCardSourceDetector()
                parser.addInterpreter(counter)
                parser.addInterpreter(detector)
                parser.parse(inputStream)
                VCardDetails(
                    estimatedType = detector.estimatedType,
                    estimatedCharset = detector.estimatedCharset,
                    version = version,
                    entryCount = counter.count,
                )
            }
        } catch (_: VCardVersionException) {
            null
        } catch (_: VCardNestedException) {
            Log.w(TAG, "Nested Exception is found (it may be false-positive).")
            null
        }
    }

    private companion object {
        private const val TAG = "ParseVCardDetails"
    }
}
