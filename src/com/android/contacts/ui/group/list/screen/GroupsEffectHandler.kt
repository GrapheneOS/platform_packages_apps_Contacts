package com.android.contacts.ui.group.list.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
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

    private fun createNewGroup(account: AccountModel?) {
        activity.startActivityForResult(
            GroupNameEditActivity.buildCreateIntent(
                activity,
                account,
                GroupsActivity::class.java,
                GroupUtil.ACTION_CREATE_GROUP,
            ),
            GroupsActivity.REQUEST_CREATE_GROUP,
        )
        close()
    }

    private fun openGroup(groupUri: Uri) {
        val intent = Intent(activity, PeopleActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(groupUri)
        activity.startActivity(intent)
        close()
    }
}
