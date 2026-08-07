package com.android.contacts.ui.settings.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class SettingsGroupUiModel(
    val id: SettingsGroupId,
    val items: ImmutableList<SettingsItemUiModel>,
    val title: String? = null,
)
