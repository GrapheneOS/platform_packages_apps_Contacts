package com.android.contacts.ui.simimport.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.ui.common.model.SelectableItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class SimImportUiState(
    val isLoading: Boolean = true,
    val accounts: ImmutableList<AccountInfo> = persistentListOf(),
    val currentAccount: AccountInfo? = null,
    val contactsToImport: ImmutableList<SelectableItem<SimContact>> = persistentListOf(),
    val contactsAlreadyImported: ImmutableList<SimContact> = persistentListOf(),
) {
    val selectedContactsCount = contactsToImport.count { it.isSelected }
    val isImportEnabled get() = selectedContactsCount > 0
    val isSelectAllEnabled get() = selectedContactsCount != contactsToImport.size
    val isDeselectAllEnabled get() = selectedContactsCount != 0
}
