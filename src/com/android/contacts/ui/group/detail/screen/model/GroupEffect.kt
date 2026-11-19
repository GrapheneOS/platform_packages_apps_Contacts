package com.android.contacts.ui.group.detail.screen.model

import android.net.Uri
import androidx.annotation.StringRes
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget

internal sealed interface GroupEffect {

    data object Close : GroupEffect

    data class OpenContact(
        val contactUri: Uri,
    ) : GroupEffect

    data class CreateContact(
        val group: Group,
    ) : GroupEffect

    data class PickMembers(
        val account: AccountModel,
        val existingMemberIds: Set<Long>,
    ) : GroupEffect

    data class EditName(
        val group: Group,
    ) : GroupEffect

    data class SendTo(
        val target: SendToGroupTarget,
        val values: List<String>,
    ) : GroupEffect

    data class PickSendTargets(
        val target: SendToGroupTarget,
        val contactIds: Set<Long>,
        val defaultSelection: Set<Long>,
    ) : GroupEffect

    data class ShowMessage(
        @param:StringRes val messageId: Int,
        val shouldSurviveClose: Boolean = false,
    ) : GroupEffect
}
