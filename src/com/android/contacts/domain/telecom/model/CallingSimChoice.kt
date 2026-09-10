package com.android.contacts.domain.telecom.model

import com.android.contacts.data.telecom.model.PhoneAccountId

internal data class CallingSimChoice(
    val dataId: Long,
    val number: String,
    val numberLabel: String?,
    val selectedAccountId: PhoneAccountId?,
)
