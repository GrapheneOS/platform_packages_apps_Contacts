package com.android.contacts.ui.interactions.showorcreate.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ShowOrCreateUiState {
    data object Searching : ShowOrCreateUiState

    data class ConfirmingCreate(
        val description: String?,
    ) : ShowOrCreateUiState
}
