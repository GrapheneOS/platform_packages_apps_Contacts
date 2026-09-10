package com.android.contacts.data.calllog.model

internal enum class CallLogEntryType {
    INCOMING,
    OUTGOING,
    MISSED,
    VOICEMAIL,
    REJECTED,
    BLOCKED,
    ANSWERED_EXTERNALLY,
    OTHER,
}
