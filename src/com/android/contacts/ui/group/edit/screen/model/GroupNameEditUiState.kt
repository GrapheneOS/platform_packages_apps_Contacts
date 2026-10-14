package com.android.contacts.ui.group.edit.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class GroupNameEditUiState(
    val isEdit: Boolean = false,
    val name: String = "",
    val isDuplicatedName: Boolean = false,
) {
    val isSaveEnabled get() = name.isNotBlank() && !isDuplicatedName
}
