package com.android.contacts.ui.selection.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.model.ContactSectionedList
import kotlinx.collections.immutable.persistentMapOf

@Immutable
internal data class ContactSelectionListState(
    val isLoading: Boolean = false,
    val searchQuery: String? = null,
    val contacts: ContactSectionedList = persistentMapOf(),
)
