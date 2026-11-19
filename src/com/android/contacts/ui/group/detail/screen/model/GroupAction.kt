package com.android.contacts.ui.group.detail.screen.model

import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget

internal sealed interface GroupAction {

    data object Dismissed : GroupAction

    data class ContactClicked(
        val key: ContactItemKey,
    ) : GroupAction

    data class ContactLongClicked(
        val key: ContactItemKey,
    ) : GroupAction

    data object SelectAllClicked : GroupAction

    data object SelectionCleared : GroupAction

    data object CreateContactClicked : GroupAction

    data object AddMembersClicked : GroupAction

    data class MembersPicked(
        val contactIds: Set<Long>,
    ) : GroupAction

    data class SendClicked(
        val target: SendToGroupTarget,
    ) : GroupAction

    data object RenameClicked : GroupAction

    data object DeleteClicked : GroupAction

    data object DeleteConfirmed : GroupAction

    data object DeleteDismissed : GroupAction

    data object RemoveSelectedClicked : GroupAction

    data class SaveCompleted(
        val action: String?,
        val isSuccessful: Boolean,
    ) : GroupAction
}
