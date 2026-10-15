package com.android.contacts.ui.vcard.cancel.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface CancelImportExportUiState {

    @Immutable
    data class Confirming(
        val type: VCardOperationType,
        val displayName: String,
    ) : CancelImportExportUiState

    @Immutable
    data object Error : CancelImportExportUiState
}
