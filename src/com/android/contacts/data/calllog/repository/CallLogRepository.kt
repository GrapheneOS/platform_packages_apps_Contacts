package com.android.contacts.data.calllog.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog.Calls
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.android.contacts.data.calllog.model.CallLogEntry
import com.android.contacts.data.calllog.model.CallLogEntryType
import com.android.contacts.di.core.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface CallLogRepository {
    suspend fun getRecentCalls(
        number: String,
        limit: Int,
    ): List<CallLogEntry>
}

internal class CallLogRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CallLogRepository {

    override suspend fun getRecentCalls(
        number: String,
        limit: Int,
    ): List<CallLogEntry> {
        if (number.isBlank() || limit <= 0) {
            return emptyList()
        }

        return withContext(ioDispatcher) {
            query(number, limit)
        }
    }

    private fun query(
        number: String,
        limit: Int,
    ): List<CallLogEntry> {
        val normalizedNumber = PhoneNumberUtils.normalizeNumber(number)

        if (normalizedNumber.isNullOrEmpty()) {
            return emptyList()
        }

        val uri = Calls.CONTENT_FILTER_URI.buildUpon()
            .appendPath(normalizedNumber)
            .appendQueryParameter(Calls.LIMIT_PARAM_KEY, limit.toString())
            .build()

        return runQuery(uri)?.use { cursor ->
            readEntries(cursor, number)
        }.orEmpty()
    }

    private fun runQuery(uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(
                uri,
                PROJECTION,
                null,
                null,
                "${Calls.DATE} DESC",
            )
        } catch (error: SecurityException) {
            Log.w(TAG, "Could not query the call log", error)
            null
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "Could not query the call log", error)
            null
        }
    }

    private fun readEntries(
        cursor: Cursor,
        number: String,
    ): List<CallLogEntry> {
        val dateColumn = cursor.getColumnIndexOrThrow(Calls.DATE)
        val durationColumn = cursor.getColumnIndexOrThrow(Calls.DURATION)
        val typeColumn = cursor.getColumnIndexOrThrow(Calls.TYPE)

        return buildList {
            while (cursor.moveToNext()) {
                add(
                    CallLogEntry(
                        number = number,
                        date = cursor.getLong(dateColumn),
                        duration = cursor.getLong(durationColumn).seconds,
                        type = toEntryType(cursor.getInt(typeColumn)),
                    ),
                )
            }
        }
    }

    private fun toEntryType(type: Int): CallLogEntryType {
        return when (type) {
            Calls.INCOMING_TYPE -> CallLogEntryType.INCOMING
            Calls.OUTGOING_TYPE -> CallLogEntryType.OUTGOING
            Calls.MISSED_TYPE -> CallLogEntryType.MISSED
            Calls.VOICEMAIL_TYPE -> CallLogEntryType.VOICEMAIL
            Calls.REJECTED_TYPE -> CallLogEntryType.REJECTED
            Calls.BLOCKED_TYPE -> CallLogEntryType.BLOCKED
            Calls.ANSWERED_EXTERNALLY_TYPE -> CallLogEntryType.ANSWERED_EXTERNALLY
            else -> CallLogEntryType.OTHER
        }
    }

    private companion object {
        const val TAG = "CallLogRepository"

        val PROJECTION = arrayOf(
            Calls.DATE,
            Calls.DURATION,
            Calls.TYPE,
        )
    }
}
