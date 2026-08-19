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
    data object NoAccounts : SimImportUiState

    @Immutable
    sealed interface WithAccounts : SimImportUiState {
        val accounts: ImmutableList<AccountUiModel>
        val currentAccount: AccountUiModel
    }

    @Immutable
    data class NoContacts(
        override val accounts: ImmutableList<AccountUiModel> = persistentListOf(),
        override val currentAccount: AccountUiModel,
    ) : WithAccounts

    @Immutable
    data class Ready(
        override val accounts: ImmutableList<AccountUiModel> = persistentListOf(),
        override val currentAccount: AccountUiModel,
        val contactsToImport: ImmutableList<SelectableItem<SimContactUiModel>> = persistentListOf(),
        val contactsAlreadyImported: ImmutableList<SimContactUiModel> = persistentListOf(),
    ) : WithAccounts {

        val selectedContactsCount = contactsToImport.count { it.isSelected }

        val isImportEnabled get() = selectedContactsCount > 0

        val isSelectAllEnabled get() = selectedContactsCount != contactsToImport.size

        val isDeselectAllEnabled get() = selectedContactsCount != 0
    }
}
