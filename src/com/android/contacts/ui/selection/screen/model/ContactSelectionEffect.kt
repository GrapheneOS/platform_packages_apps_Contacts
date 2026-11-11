package com.android.contacts.ui.selection.screen.model

import android.net.Uri

internal sealed interface ContactSelectionEffect {

    data class Close(
        val result: ContactSelectionResult,
    ) : ContactSelectionEffect

    sealed interface ForwardToEditor : ContactSelectionEffect {

        data object CreateContact : ForwardToEditor

        data class EditContact(
            val lookupUri: Uri,
        ) : ForwardToEditor
    }

    data class SendTo(
        val target: SentToGroupTarget,
        val addresses: List<String>,
        val chooserTitle: String?,
    ) : ContactSelectionEffect
}
