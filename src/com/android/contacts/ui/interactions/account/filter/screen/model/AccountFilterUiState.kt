package com.android.contacts.ui.interactions.account.filter.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.model.SelectableItem
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal sealed interface AccountFilterUiState {

    data object Loading : AccountFilterUiState

    data class Ready(
        val items: ImmutableList<SelectableItem<AccountFilterItem>>,
    ) : AccountFilterUiState
}
