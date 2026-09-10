package com.android.contacts.ui.editor.springboard.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal sealed interface ContactEditorSpringBoardUiState {

    @Immutable
    data object Loading : ContactEditorSpringBoardUiState

    @Immutable
    sealed interface ShowContactsDialog : ContactEditorSpringBoardUiState {
        val contacts: ImmutableList<RawContactUiModel>
    }

    @Immutable
    data class ShowPickContactToEdit(
        override val contacts: ImmutableList<RawContactUiModel>,
    ) : ShowContactsDialog

    @Immutable
    data class ShowLinkedContacts(
        override val contacts: ImmutableList<RawContactUiModel>,
        val isUserProfile: Boolean,
    ) : ShowContactsDialog

    @Immutable
    data object ShowUnlinkConfirmation : ContactEditorSpringBoardUiState
}
