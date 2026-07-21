package com.android.contacts.ui.simimport.screen.model

internal sealed interface SimImportEffect {
    data class Close(
        val isSuccessful: Boolean,
    ) : SimImportEffect
}
