package com.android.contacts.ui.simimport.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.model.SimContact
import com.android.contacts.ui.common.model.SelectableItem
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class SimImportUiState(
    val accounts: ImmutableList<AccountUiModel>? = null,
    val currentAccount: AccountUiModel? = null,
    val contactsToImport: ImmutableList<SelectableItem<SimContact>>? = null,
    val contactsAlreadyImported: ImmutableList<SimContact>? = null,
) {
    val isLoading
        get() = accounts == null || contactsToImport == null || contactsAlreadyImported == null

    val showEmptyState
        get() = !isLoading && (
            accounts?.isEmpty() == true ||
                (contactsToImport?.isEmpty() == true && contactsAlreadyImported?.isEmpty() == true)
            )

    val selectedContactsCount = contactsToImport?.count { it.isSelected } ?: 0

    val isImportEnabled get() = selectedContactsCount > 0

    val isSelectAllEnabled
        get() = contactsToImport != null && selectedContactsCount != contactsToImport.size

    val isDeselectAllEnabled get() = selectedContactsCount != 0
}
