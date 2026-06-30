package com.android.contacts.ui.simimport.screen.model

import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo

internal sealed interface SimImportAction {
    data object CloseClicked : SimImportAction
    data class AccountChanged(val account: AccountInfo) : SimImportAction
    data class ContactClicked(val contact: SimContact) : SimImportAction
    data object SelectAllClicked : SimImportAction
    data object DeselectAllClicked : SimImportAction
    data object ImportClicked : SimImportAction
}
