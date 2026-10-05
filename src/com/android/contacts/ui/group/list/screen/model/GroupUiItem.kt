package com.android.contacts.ui.group.list.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class GroupUiItem(
    val id: Long,
    val name: String,
    val summaryCount: Int,
)
