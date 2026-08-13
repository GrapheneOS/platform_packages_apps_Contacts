package com.android.contacts.ui.simimport.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.model.SelectableItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal sealed interface SimImportUiState {

    @Immutable
    data object Loading : SimImportUiState

    @Immutable
    sealed interface Empty : SimImportUiState {
        @Immutable
        data object NoAccounts : Empty

        @Immutable
        data object NoContacts : Empty
    }

    @Immutable
    data class Ready(
        val accounts: ImmutableList<AccountUiModel> = persistentListOf(),
        val currentAccount: AccountUiModel,
        val contactsToImport: ImmutableList<SelectableItem<SimContactUiModel>> = persistentListOf(),
        val contactsAlreadyImported: ImmutableList<SimContactUiModel> = persistentListOf(),
    ) : SimImportUiState {

        val selectedContactsCount = contactsToImport.count { it.isSelected }

        val isImportEnabled get() = selectedContactsCount > 0

        val isSelectAllEnabled get() = selectedContactsCount != contactsToImport.size

        val isDeselectAllEnabled get() = selectedContactsCount != 0
    }
}
