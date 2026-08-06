package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsItemUiModel(
    val id: SettingsItemId,
    val title: String,
    val summary: String? = null,
)
