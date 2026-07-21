package com.android.contacts.ui.simimport.screen.model

import com.android.contacts.model.SimContact

internal sealed interface SimImportAction {
    data object CloseClicked : SimImportAction
    data class AccountChanged(
        val account: AccountUiModel,
    ) : SimImportAction
    data class ContactSelectionChanged(
        val contact: SimContact,
        val isSelected: Boolean,
    ) : SimImportAction

    data object SelectAllClicked : SimImportAction
    data object DeselectAllClicked : SimImportAction
    data object ImportClicked : SimImportAction
}
