package com.android.contacts.domain.vcard.usecase

import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.vcard.ExportRequest
import com.android.contacts.vcard.NotificationImportExportListener
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take

internal fun interface ExportVCard {
    operator fun invoke(request: ExportRequest): Flow<Boolean>
}

internal class ExportVCardImpl @Inject constructor(
    private val deleteExportFiles: DeleteExportFiles,
    private val vCardServiceRunner: VCardServiceRunner,
    private val notificationImportExportListener: NotificationImportExportListener,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : ExportVCard {

    override fun invoke(request: ExportRequest): Flow<Boolean> {
        return flow {
            deleteExportFiles()

            var requestHandled = false

            vCardServiceRunner()
                .take(1)
                .collect { vCardService ->
                    vCardService.handleExportRequest(
                        request,
                        notificationImportExportListener,
                    )
                    requestHandled = true
                }

            emit(requestHandled)
        }
            .catch {
                if (it is CancellationException) {
                    throw it
                }

                Log.w(TAG, "Export service failure", it)
                emit(false)
            }
            .flowOn(coroutineDispatcher)
    }

    private companion object {
        const val TAG = "ExportVCards"
    }
}
