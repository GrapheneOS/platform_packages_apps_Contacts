package com.android.contacts.ui.simimport.screen.model

import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.ui.common.model.SelectableItem

internal data class SimImportUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountInfo> = emptyList(),
    val currentAccount: AccountInfo? = null,
    val contactsToImport: List<SelectableItem<SimContact>> = emptyList(),
    val contactsAlreadyImported: List<SimContact> = emptyList(),
) {
    val selectedContactsCount = contactsToImport.count { it.isSelected }
    val isImportEnabled get() = selectedContactsCount > 0
    val isSelectAllEnabled get() = selectedContactsCount != contactsToImport.size
    val isDeselectAllEnabled get() = selectedContactsCount != 0
}
