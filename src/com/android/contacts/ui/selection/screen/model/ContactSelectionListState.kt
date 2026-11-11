package com.android.contacts.ui.selection.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Immutable
internal data class ContactSelectionListState(
    val isLoading: Boolean = false,
    val searchQuery: String? = null,
    val contacts: ImmutableMap<ContactListSection, ImmutableList<ContactUiModel>> =
        persistentMapOf(),
)
