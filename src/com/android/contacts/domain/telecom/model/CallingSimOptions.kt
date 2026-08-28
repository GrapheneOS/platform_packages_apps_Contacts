package com.android.contacts.domain.telecom.model

import com.android.contacts.data.telecom.model.CallingSim

internal data class CallingSimOptions(
    val sims: List<CallingSim>,
    val choices: List<CallingSimChoice>,
)
