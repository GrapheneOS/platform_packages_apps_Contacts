package com.android.contacts.ui.vcard.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal enum class ImportVCardUiState {
    PREPARING,
    IMPORTING,
    CANCELLING,
}
