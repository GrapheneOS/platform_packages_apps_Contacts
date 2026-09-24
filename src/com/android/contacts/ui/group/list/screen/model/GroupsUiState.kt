package com.android.contacts.ui.group.list.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal sealed interface GroupsUiState {

    data object Loading : GroupsUiState

    data object WithoutAccounts : GroupsUiState

    data class WithAccounts(
        val groups: ImmutableList<AccountGroupsItem>,
        val showAccountHeaders: Boolean,
    ) : GroupsUiState
}
