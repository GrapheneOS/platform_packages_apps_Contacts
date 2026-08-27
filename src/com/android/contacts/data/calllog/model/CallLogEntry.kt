package com.android.contacts.data.calllog.model

import kotlin.time.Duration

internal data class CallLogEntry(
    val number: String,
    val date: Long,
    val duration: Duration,
    val type: CallLogEntryType,
)
