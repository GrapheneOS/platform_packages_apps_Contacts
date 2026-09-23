package com.android.contacts.ui.group.edit.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface GroupNameEditUiState {
    @Immutable
    data object Loading : GroupNameEditUiState

    @Immutable
    data class Ready(
        val isEdit: Boolean = false,
        val name: String = "",
        val maxLenght: Int,
        val inputError: GroupNameEditInputError? = null,
    ) : GroupNameEditUiState {
        val isOkEnabled get() = name.isNotBlank() && inputError == null
    }
}
