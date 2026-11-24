package com.android.contacts.ui.selection.screen.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
internal data class ContactSelectionUiState(
    val title: String = "",
    val description: String? = null,
    val listHeader: String? = null,
    @param:StringRes val emptyMessageId: Int? = null,
    val listState: ContactSelectionListState = ContactSelectionListState(isLoading = true),
    val searchState: ContactSelectionSearchState = ContactSelectionSearchState.Unavailable,
    val allowMultiple: Boolean = false,
    val selectedCount: Int = 0,
    val canCreate: Boolean = false,
    val showAvatars: Boolean = true,
)
