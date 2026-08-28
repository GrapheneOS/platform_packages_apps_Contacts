package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.data.telecom.model.PhoneAccountId

@Immutable
internal data class CallingSimNumberUiModel(
    val dataId: Long,
    val number: String,
    val numberLabel: String?,
    val selectedAccountId: PhoneAccountId?,
)
