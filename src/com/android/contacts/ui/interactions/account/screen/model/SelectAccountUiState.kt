package com.android.contacts.ui.interactions.account.screen.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class SelectAccountUiState(
    @param:StringRes val titleId: Int? = null,
    val accounts: ImmutableList<AccountUiModel>? = null,
) {
    val isLoading get() = accounts == null
}
