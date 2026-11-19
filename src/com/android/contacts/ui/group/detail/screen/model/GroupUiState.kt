package com.android.contacts.ui.group.detail.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.model.ContactSectionedList

@Immutable
internal sealed interface GroupUiState {

    data object Loading : GroupUiState

    data class Content(
        val title: String,
        val subtitle: String?,
        val contacts: ContactSectionedList,
        val isSelectionMode: Boolean,
        val canAddMembers: Boolean,
        val canRemoveMembers: Boolean,
        val canEditGroup: Boolean,
        val canSend: Boolean,
        val canSelectAll: Boolean,
        val deleteConfirmationMessage: String?,
    ) : GroupUiState
}
