package com.android.contacts.ui.group.list.screen.model

import android.net.Uri
import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface GroupsEffect {

    data object Close : GroupsEffect

    data class CreateNewGroup(
        val account: AccountModel?,
    ) : GroupsEffect

    data class OpenGroup(
        val groupUri: Uri,
    ) : GroupsEffect
}
