package com.android.contacts.ui.vcardexport.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class ExportVCardUiState(
    val showModeDialog: Boolean = false,
    val availableModes: ImmutableSet<ExportMode> = persistentSetOf(),
)
