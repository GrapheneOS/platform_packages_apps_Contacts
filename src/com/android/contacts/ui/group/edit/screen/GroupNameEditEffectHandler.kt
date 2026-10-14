package com.android.contacts.ui.group.edit.screen

import android.app.Activity
import android.content.Intent
import com.android.contacts.ContactSaveService
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect

internal interface GroupNameEditEffectHandler {
    fun handle(effect: Effect)
}

internal class GroupNameEditEffectHandlerImpl(
    private val accountModelMapper: AccountModelMapper,
    private val activity: Activity,
    private val callbackActivity: Class<out Activity>,
    private val callbackAction: String?,
) : GroupNameEditEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> onClose(effect.isSuccessful)
            is Effect.CreateGroup -> onCreateGroup(effect.account, effect.groupName)
            is Effect.EditGroup -> onEditGroup(effect.groupId, effect.groupName)
        }
    }

    private fun onClose(isSuccessful: Boolean) {
        activity.setResult(
            when {
                isSuccessful -> Activity.RESULT_OK
                else -> Activity.RESULT_CANCELED
            },
        )
        activity.finish()
    }

    private fun onCreateGroup(account: AccountModel?, groupName: String) {
        startContactSaveService(
            ContactSaveService.createNewGroupIntent(
                activity,
                account?.let(accountModelMapper::map),
                groupName,
                null,
                callbackActivity,
                callbackAction,
            ),
        )
        onClose(isSuccessful = true)
    }

    private fun onEditGroup(groupId: Long, groupName: String) {
        startContactSaveService(
            ContactSaveService.createGroupRenameIntent(
                activity,
                groupId,
                groupName,
                callbackActivity,
                callbackAction,
            ),
        )
        onClose(isSuccessful = true)
    }

    private fun startContactSaveService(intent: Intent) {
        ContactSaveService.startService(activity, intent)
    }
}
