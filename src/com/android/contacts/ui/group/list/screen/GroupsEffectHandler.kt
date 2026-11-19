package com.android.contacts.ui.group.list.screen

import android.app.Activity
import android.net.Uri
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.list.GroupsActivity
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect

internal interface GroupsEffectHandler {
    fun handle(effect: Effect)
}

internal class GroupsEffectHandlerImpl(
    private val activity: Activity,
) : GroupsEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> close()
            is Effect.CreateNewGroup -> createNewGroup(effect.account)
            is Effect.OpenGroup -> openGroup(effect.groupUri)
        }
    }

    private fun close() {
        activity.finish()
    }

    private fun createNewGroup(account: AccountModel) {
        activity.startActivity(
            GroupNameEditActivity.buildCreateIntent(
                activity,
                account,
                GroupsActivity::class.java,
                GroupUtil.ACTION_CREATE_GROUP,
            ),
        )
    }

    private fun openGroup(groupUri: Uri) {
        val intent = GroupActivity.buildIntent(activity, groupUri)
        activity.startActivity(intent)
        close()
    }
}
