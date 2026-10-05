package com.android.contacts.ui.group.edit.screen.model

internal sealed interface GroupNameEditEffect {
    data class Close(
        val isSuccessful: Boolean,
    ) : GroupNameEditEffect
}
