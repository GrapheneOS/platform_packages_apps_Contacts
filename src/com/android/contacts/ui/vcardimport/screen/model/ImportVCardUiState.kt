package com.android.contacts.ui.vcardimport.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ImportVCardUiState {
    data object Preparing : ImportVCardUiState
    data object Importing : ImportVCardUiState
    data object Cancelling : ImportVCardUiState
}
