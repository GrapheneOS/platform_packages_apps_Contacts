package com.android.contacts.ui.simimport.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel

@Immutable
internal data class AccountUiModel(
    val account: AccountModel,
    val name: String?,
    val type: String? = null,
    val iconData: AccountIconData? = null,
)
