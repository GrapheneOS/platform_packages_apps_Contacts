package com.android.contacts.domain.calllog.model

import com.android.contacts.data.calllog.model.CallLogEntryType
import kotlin.time.Duration

internal data class RecentCall(
    val number: String,
    val numberLabel: String?,
    val date: Long,
    val duration: Duration,
    val type: CallLogEntryType,
)
