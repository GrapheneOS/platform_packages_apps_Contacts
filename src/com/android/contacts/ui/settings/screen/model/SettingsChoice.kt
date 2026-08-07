package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SettingsChoice<T>(
    val value: T,
    val label: String,
)
