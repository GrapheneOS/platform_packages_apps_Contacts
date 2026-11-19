package com.android.contacts.ui.group.detail.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import com.android.contacts.R
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.group.GroupUtil
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.detail.screen.model.GroupEffect as Effect
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.selection.ContactSelectionIntents
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import com.android.contacts.util.ImplicitIntentsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface GroupEffectHandler {
    fun handle(effect: Effect)
}

internal class GroupEffectHandlerImpl(
    private val activity: Activity,
    private val pickMembersLauncher: ActivityResultLauncher<Intent>,
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
) : GroupEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            Effect.Close -> activity.finish()
            is Effect.OpenContact -> openContact(effect.contactUri)
            is Effect.CreateContact -> createContact(effect.group)
            is Effect.PickMembers -> pickMembers(effect)
            is Effect.EditName -> editName(effect.group)
            is Effect.SendTo -> sendTo(effect)
            is Effect.PickSendTargets -> pickSendTargets(effect)
            is Effect.ShowMessage -> showMessage(effect.messageId, effect.shouldSurviveClose)
        }
    }

    private fun openContact(contactUri: Uri) {
        ImplicitIntentsUtil.startQuickContact(activity, contactUri)
    }

    private fun createContact(group: Group) {
        // TODO: Pre-add the new contact to the group, once the contact editor supports it
        val account = group.account
        val intent = Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI)
            .putExtra(
                ContactEditorActivity.EXTRA_ACCOUNT_WITH_DATA_SET,
                AccountWithDataSet(account.name, account.type, account.dataSet),
            )
        startActivity(intent)
    }

    private fun pickMembers(effect: Effect.PickMembers) {
        val intent = ContactSelectionIntents.addGroupMembers(
            context = activity,
            account = effect.account,
            existingMemberIds = effect.existingMemberIds,
        )
        try {
            pickMembersLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            handleActivityNotFound(intent, e)
        }
    }

    private fun editName(group: Group) {
        startActivity(
            GroupNameEditActivity.buildEditIntent(
                context = activity,
                groupId = group.id,
                groupName = group.name,
                account = group.account,
                callbackActivity = GroupActivity::class.java,
                callbackAction = GroupUtil.ACTION_UPDATE_GROUP,
            ),
        )
    }

    private fun sendTo(effect: Effect.SendTo) {
        val uri = Uri.fromParts(effect.target.scheme, effect.values.joinToString(","), null)
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(sendIntent, chooserTitle(effect.target)))
    }

    private fun pickSendTargets(effect: Effect.PickSendTargets) {
        startActivity(
            ContactSelectionIntents.sendToGroup(
                context = activity,
                target = effect.target,
                contactIds = effect.contactIds,
                defaultSelection = effect.defaultSelection,
                chooserTitle = chooserTitle(effect.target),
            ),
        )
    }

    private fun chooserTitle(target: SendToGroupTarget): String {
        return activity.getString(
            when (target) {
                SendToGroupTarget.SMS -> R.string.menu_sendMessageOption
                SendToGroupTarget.EMAIL -> R.string.menu_sendEmailOption
            },
        )
    }

    private fun showMessage(
        messageId: Int,
        shouldSurviveClose: Boolean = false,
    ) {
        if (shouldSurviveClose) {
            Toast.makeText(activity.applicationContext, messageId, Toast.LENGTH_SHORT).show()
            return
        }
        val message = activity.getString(messageId)
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    private fun startActivity(intent: Intent) {
        try {
            ImplicitIntentsUtil.startActivityInAppIfPossible(activity, intent)
        } catch (e: ActivityNotFoundException) {
            handleActivityNotFound(intent, e)
        }
    }

    private fun handleActivityNotFound(
        intent: Intent,
        exception: ActivityNotFoundException,
    ) {
        Log.e(TAG, "Could not start $intent", exception)
        showMessage(R.string.missing_app)
    }

    private companion object {
        const val TAG = "GroupEffectHandler"
    }
}
