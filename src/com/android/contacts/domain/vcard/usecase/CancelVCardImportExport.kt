package com.android.contacts.domain.vcard.usecase

import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.vcard.CancelRequest
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

internal fun interface CancelVCardImportExport {
    suspend operator fun invoke(jobId: Int, displayName: String): Boolean
}

internal class CancelVCardImportExportImpl @Inject constructor(
    private val vCardServiceRunner: VCardServiceRunner,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : CancelVCardImportExport {

    override suspend fun invoke(jobId: Int, displayName: String): Boolean {
        return withContext(coroutineDispatcher) {
            var requestHandled = false

            @Suppress("TooGenericExceptionCaught")
            try {
                vCardServiceRunner()
                    .take(1)
                    .collect { vCardService ->
                        vCardService.handleCancelRequest(
                            CancelRequest(jobId, displayName),
                            null,
                        )
                        requestHandled = true
                    }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                Log.w(TAG, "Cancel VCard import/export failure", e)
            }

            requestHandled
        }
    }

    private companion object {
        const val TAG = "CanceVCardImportExport"
    }
}
