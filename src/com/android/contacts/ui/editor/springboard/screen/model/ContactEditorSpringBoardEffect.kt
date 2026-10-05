package com.android.contacts.ui.editor.springboard.screen.model

import android.net.Uri

internal sealed interface ContactEditorSpringBoardEffect {

    data object Close : ContactEditorSpringBoardEffect

    data object ShowErrorAndClose : ContactEditorSpringBoardEffect

    data class EditContact(
        val uri: Uri,
        val rawContactId: Long,
    ) : ContactEditorSpringBoardEffect

    data class CreateContact(
        val uri: Uri,
    ) : ContactEditorSpringBoardEffect

    data class SelectContactToJoin(
        val contactId: Long,
    ) : ContactEditorSpringBoardEffect

    data class UnlinkRawContacts(
        val rawContactIds: List<Long>,
    ) : ContactEditorSpringBoardEffect

    data class JoinContacts(
        val contactId1: Long,
        val contactId2: Long,
    ) : ContactEditorSpringBoardEffect
}
