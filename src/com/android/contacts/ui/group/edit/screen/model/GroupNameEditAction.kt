package com.android.contacts.ui.group.edit.screen.model

internal sealed interface GroupNameEditAction {

    data object Dismissed : GroupNameEditAction

    data class NameChanged(
        val value: String,
    ) : GroupNameEditAction

    data object OkClicked : GroupNameEditAction
}
