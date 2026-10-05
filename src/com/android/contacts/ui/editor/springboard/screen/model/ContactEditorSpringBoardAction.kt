package com.android.contacts.ui.editor.springboard.screen.model

import android.net.Uri

internal sealed interface ContactEditorSpringBoardAction {

    data object CloseClicked : ContactEditorSpringBoardAction

    data class ContactClicked(
        val rawContactId: Long,
    ) : ContactEditorSpringBoardAction

    data object UnlinkClicked : ContactEditorSpringBoardAction

    data object UnlinkConfirmed : ContactEditorSpringBoardAction

    data object UnlinkDismissed : ContactEditorSpringBoardAction

    data object AddClicked : ContactEditorSpringBoardAction

    data class AddContactSelected(
        val contactUri: Uri,
    ) : ContactEditorSpringBoardAction

    data object DialogDismissed : ContactEditorSpringBoardAction
}
