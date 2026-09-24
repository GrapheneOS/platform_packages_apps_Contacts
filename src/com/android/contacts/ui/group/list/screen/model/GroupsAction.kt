package com.android.contacts.ui.group.list.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface GroupsAction {

    data object Dismissed : GroupsAction

    data class NewClicked(
        val account: AccountModel?,
    ) : GroupsAction

    data class GroupClicked(
        val group: GroupUiItem,
    ) : GroupsAction
}
