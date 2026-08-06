package com.android.contacts.data.simimport.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.contacts.SimImportService
import com.android.contacts.data.simimport.model.SimImportResult
import com.android.contacts.util.core.CurrentTimeProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal interface SimImportResultRepository {
    fun observeSimImportResults(): Flow<SimImportResult>
}

internal class SimImportResultRepositoryImpl @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val currentTimeProvider: CurrentTimeProvider,
) : SimImportResultRepository {

    override fun observeSimImportResults(): Flow<SimImportResult> {
        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val result = intent?.let(::toSimImportResult) ?: return
                    trySend(result)
                }
            }

            localBroadcastManager.registerReceiver(
                receiver,
                IntentFilter(SimImportService.BROADCAST_SIM_IMPORT_COMPLETE),
            )

            awaitClose {
                localBroadcastManager.unregisterReceiver(receiver)
            }
        }
    }

    private fun toSimImportResult(intent: Intent): SimImportResult? {
        if (isStale(intent)) {
            return null
        }

        val resultCode = intent.getIntExtra(
            SimImportService.EXTRA_RESULT_CODE,
            SimImportService.RESULT_UNKNOWN,
        )

        return when (resultCode) {
            SimImportService.RESULT_SUCCESS -> toSuccessResult(intent)
            SimImportService.RESULT_FAILURE -> SimImportResult.Failure
            else -> null
        }
    }

    private fun toSuccessResult(intent: Intent): SimImportResult? {
        val importedCount = intent.getIntExtra(
            SimImportService.EXTRA_RESULT_COUNT,
            UNKNOWN_COUNT,
        )

        return when {
            importedCount > 0 -> SimImportResult.Success(importedCount)
            else -> null
        }
    }

    private fun isStale(intent: Intent): Boolean {
        val now = currentTimeProvider.currentTimeMillis()
        val requestedAt = intent.getLongExtra(
            SimImportService.EXTRA_OPERATION_REQUESTED_AT_TIME,
            now,
        )

        return now - requestedAt > RESULT_MAX_AGE_MILLIS
    }

    private companion object {
        const val UNKNOWN_COUNT = -1
        const val RESULT_MAX_AGE_MILLIS = 30_000L
    }
}
