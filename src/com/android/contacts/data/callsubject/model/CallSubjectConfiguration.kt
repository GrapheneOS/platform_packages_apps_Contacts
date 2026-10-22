package com.android.contacts.data.callsubject.model

import java.nio.charset.Charset

internal data class CallSubjectConfiguration(
    val limit: Int = DEFAULT_CALL_SUBJECT_LIMIT,
    val encoding: Charset? = null,
)

internal const val DEFAULT_CALL_SUBJECT_LIMIT = 16
