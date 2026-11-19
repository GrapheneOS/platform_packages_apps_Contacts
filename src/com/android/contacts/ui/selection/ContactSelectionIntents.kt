package com.android.contacts.ui.selection

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Groups
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.list.UiIntentActions
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget

internal object ContactSelectionIntents {

    fun addGroupMembers(
        context: Context,
        account: AccountModel,
        existingMemberIds: Collection<Long>,
    ): Intent {
        return Intent(context, ContactSelectionActivity::class.java)
            .setAction(Intent.ACTION_PICK)
            .setType(Groups.CONTENT_TYPE)
            .putExtra(UiIntentActions.GROUP_ACCOUNT_NAME, account.name)
            .putExtra(UiIntentActions.GROUP_ACCOUNT_TYPE, account.type)
            .putExtra(UiIntentActions.GROUP_ACCOUNT_DATA_SET, account.dataSet)
            .putStringArrayListExtra(
                UiIntentActions.GROUP_CONTACT_IDS,
                existingMemberIds.mapTo(ArrayList()) { it.toString() },
            )
    }

    fun getPickedGroupMemberIds(result: Intent?): Set<Long> {
        val contactIds = result?.getLongArrayExtra(UiIntentActions.TARGET_CONTACT_IDS_EXTRA_KEY)
        val contactId =
            result?.getLongExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, NO_ID) ?: NO_ID
        return when {
            contactIds != null -> contactIds.toSet()
            contactId != NO_ID -> setOf(contactId)
            else -> emptySet()
        }
    }

    fun sendToGroup(
        context: Context,
        target: SendToGroupTarget,
        contactIds: Collection<Long>,
        defaultSelection: Collection<Long>,
        chooserTitle: String?,
    ): Intent {
        return Intent(context, ContactSelectionActivity::class.java)
            .setAction(UiIntentActions.ACTION_SELECT_ITEMS)
            .setType(
                when (target) {
                    SendToGroupTarget.SMS -> Phone.CONTENT_TYPE
                    SendToGroupTarget.EMAIL -> Email.CONTENT_TYPE
                },
            )
            .putExtra(UiIntentActions.SELECTION_ITEM_LIST, contactIds.toLongArray())
            .putExtra(UiIntentActions.SELECTION_DEFAULT_SELECTION, defaultSelection.toLongArray())
            .putExtra(UiIntentActions.SELECTION_SEND_SCHEME, target.scheme)
            .putExtra(UiIntentActions.SELECTION_SEND_TITLE, chooserTitle)
    }

    private const val NO_ID = -1L
}
