package com.android.contacts.ui.group.list.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class AccountGroupsItem(
    val account: AccountModel?,
    val accountName: String,
    val canCreateGroup: Boolean,
    val groups: ImmutableList<GroupUiItem>,
)
