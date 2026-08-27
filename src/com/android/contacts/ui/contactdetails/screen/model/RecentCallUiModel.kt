package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class RecentCallUiModel(
    val title: String,
    val numberLabel: String?,
    val date: String,
    val direction: RecentCallDirection,
    val contentDescription: String,
)
