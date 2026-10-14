package com.android.contacts.ui.group.edit.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface GroupNameEditEffect {
    data class Close(
        val isSuccessful: Boolean,
    ) : GroupNameEditEffect

    data class CreateGroup(
        val account: AccountModel?,
        val groupName: String,
    ) : GroupNameEditEffect

    data class EditGroup(
        val groupId: Long,
        val groupName: String,
    ) : GroupNameEditEffect
}
