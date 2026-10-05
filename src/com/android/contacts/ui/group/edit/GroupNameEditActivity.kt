package com.android.contacts.ui.group.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.group.edit.screen.GroupNameEditEffectHandlerImpl
import com.android.contacts.ui.group.edit.screen.GroupNameEditScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupNameEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = GroupNameEditEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                GroupNameEditScreen(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    companion object {
        const val EXTRA_GROUP_ID = "group_id"
        const val EXTRA_GROUP_NAME = "group_name"
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_CALLBACK_ACTIVITY = "callback_activity"
        const val EXTRA_CALLBACK_ACTION = "calvlback_action"

        internal fun buildCreateIntent(
            context: Context,
            account: AccountModel? = null,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            return Intent(context, GroupNameEditActivity::class.java)
                .putCommonExtras(account, callbackActivity, callbackAction)
        }

        internal fun buildEditIntent(
            context: Context,
            groupId: Long,
            groupName: String,
            account: AccountModel? = null,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            return Intent(context, GroupNameEditActivity::class.java)
                .putExtra(EXTRA_GROUP_ID, groupId)
                .putExtra(EXTRA_GROUP_NAME, groupName)
                .putCommonExtras(account, callbackActivity, callbackAction)
        }

        private fun Intent.putCommonExtras(
            account: AccountModel? = null,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            account?.let { putExtra(EXTRA_ACCOUNT, it) }
            return putExtra(EXTRA_CALLBACK_ACTIVITY, callbackActivity)
                .putExtra(EXTRA_CALLBACK_ACTION, callbackAction)
        }
    }
}
