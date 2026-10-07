package com.android.contacts.domain.vcard.usecase

import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.vcard.model.ImportVCardError as Error
import com.android.contacts.domain.vcard.model.ImportVCardSource as Source
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.util.core.AcquireWakeLock
import com.android.contacts.vcard.ImportRequest
import com.android.contacts.vcard.NotificationImportExportListener
import com.android.vcard.exception.VCardException
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take

internal fun interface ImportVCards {
    operator fun invoke(account: AccountModel, sources: List<Source>): Flow<Error>
}

internal class ImportVCardsImpl @Inject constructor(
    private val acquireWakeLock: AcquireWakeLock,
    private val parseVCardDetails: ParseVCardDetails,
    private val vCardServiceRunner: VCardServiceRunner,
    private val notificationImportExportListener: NotificationImportExportListener,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : ImportVCards {

    override fun invoke(
        account: AccountModel,
        sources: List<Source>,
    ): Flow<Error> {
        return flow {
            withWakeLock {
                val requests = prepareRequests(
                    account = account,
                    sources = sources,
                    onError = { emit(it) },
                )

                if (requests.isEmpty()) {
                    Log.w(TAG, "Empty import requests. Ignore it.")
                    return@withWakeLock
                }

                var requestHandled = false

                vCardServiceRunner()
                    .take(1)
                    .collect { vCardService ->
                        vCardService.handleImportRequest(
                            requests,
                            notificationImportExportListener,
                        )
                        requestHandled = true
                    }

                if (!requestHandled) {
                    emit(Error.Unknown)
                }
            }
        }
            .catch {
                when (it) {
                    is CancellationException -> {
                        throw it
                    }
                    is OutOfMemoryError -> {
                        System.gc()
                        emit(Error.OutOfMemory)
                    }
                    is IOException -> {
                        emit(Error.Io)
                    }
                    else -> {
                        emit(Error.Unknown)
                    }
                }

                Log.w(TAG, "Error importing vCards", it)
            }
            .flowOn(coroutineDispatcher)
    }

    private suspend fun <T> withWakeLock(callback: suspend () -> T) {
        val wakeLock = acquireWakeLock(
            tag = TAG_WAKE_LOCK,
            timeout = 30.seconds,
        )
        try {
            callback()
        } finally {
            wakeLock.releaseIfHeld()
        }
    }

    private suspend inline fun prepareRequests(
        account: AccountModel,
        sources: List<Source>,
        onError: (Error) -> Unit,
    ): List<ImportRequest> {
        return sources.mapNotNull { source ->
            val details = try {
                parseVCardDetails(source.uri) ?: return@mapNotNull null
            } catch (e: VCardException) {
                Log.e(TAG, "Failed to parse vcard details", e)
                onError(Error.NotSupported)
                return@mapNotNull null
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Failed to parse vcard details", e)
                System.gc()
                onError(Error.OutOfMemory)
                return@mapNotNull null
            } catch (e: IOException) {
                Log.e(TAG, "Failed to parse vcard details", e)
                onError(Error.Io)
                return@mapNotNull null
            }

            ImportRequest(
                AccountWithDataSet(account.name, account.type, account.dataSet),
                null,
                source.uri,
                source.name,
                details.estimatedType,
                details.estimatedCharset,
                details.version.value,
                details.entryCount,
            )
        }
    }

    private companion object {
        const val TAG = "ImportVCards"
        const val TAG_WAKE_LOCK = "contacts:import_vcards"
    }
}
