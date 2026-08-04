package com.android.contacts.domain.vcard.usecase

import android.content.Context
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.util.AcquireWakeLock
import com.android.contacts.domain.vcard.model.ImportVCardSource as Source
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.vcard.screen.model.ImportVCardError as Error
import com.android.contacts.vcard.ImportRequest
import com.android.contacts.vcard.NotificationImportExportListener
import com.android.vcard.exception.VCardException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal fun interface ImportVCards {
    operator fun invoke(account: AccountModel, sources: List<Source>): Flow<Error>
}

internal class ImportVCardsImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
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

                // Start a new coroutine so we can cancel it in isolation
                CoroutineScope(coroutineDispatcher).launch {
                    vCardServiceRunner().collect { vCardService ->
                        try {
                            vCardService.handleImportRequest(
                                requests,
                                notificationImportExportListener,
                            )
                        } catch (e: OutOfMemoryError) {
                            Log.e(TAG, "OutOfMemoryError occured during caching vCard", e)
                            System.gc()
                            emit(Error.OutOfMemory)
                        } catch (e: IOException) {
                            Log.e(TAG, "IOException during caching vCard", e)
                            emit(Error.Io)
                        }

                        // Cancel to unbind service
                        this.coroutineContext.job.cancel()
                    }
                }.join()
            }
        }.flowOn(coroutineDispatcher)
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
