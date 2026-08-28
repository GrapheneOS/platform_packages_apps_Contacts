package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class CallingSimUiModel(
    val accounts: ImmutableList<CallingSimAccountUiModel>,
    val numbers: ImmutableList<CallingSimNumberUiModel>,
)
