package com.android.contacts.ui.selection.screen.model

import android.net.Uri

internal sealed interface ContactSelectionResult {

    data class Picked(
        val uri: Uri,
    ) : ContactSelectionResult

    data class PickedMultiple(
        val uris: List<Uri>,
        val mimeType: String,
    ) : ContactSelectionResult

    data class Shortcut(
        val target: ShortcutTarget,
        val uri: Uri,
    ) : ContactSelectionResult

    data class GroupMember(
        val contactId: Long,
    ) : ContactSelectionResult

    data class GroupMembers(
        val contactIds: Set<Long>,
    ) : ContactSelectionResult

    data object Canceled : ContactSelectionResult
}
