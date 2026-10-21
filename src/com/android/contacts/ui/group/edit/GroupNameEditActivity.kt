package com.android.contacts.ui.group.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.group.edit.screen.GroupNameEditEffectHandlerImpl
import com.android.contacts.ui.group.edit.screen.GroupNameEditScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupNameEditActivity : ComponentActivity() {

    @Inject
    internal lateinit var accountModelMapper: AccountModelMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("UNCHECKED_CAST")
        val callbackActivity = intent.getSerializableExtra(
            EXTRA_CALLBACK_ACTIVITY,
            Class::class.java,
        ) as? Class<out Activity> ?: run {
            Log.e(TAG, "Calling activity class missing")
            finish()
            return
        }
        val callbackAction = intent.getStringExtra(EXTRA_CALLBACK_ACTION)

        val effectHandler = GroupNameEditEffectHandlerImpl(
            accountModelMapper = accountModelMapper,
            activity = this,
            callbackActivity = callbackActivity,
            callbackAction = callbackAction,
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
        private const val TAG = "GroupNameEditActivity"

        const val EXTRA_GROUP_ID = "group_id"
        const val EXTRA_GROUP_NAME = "group_name"
        const val EXTRA_ACCOUNT = "account"
        private const val EXTRA_CALLBACK_ACTIVITY = "callback_activity"
        private const val EXTRA_CALLBACK_ACTION = "callback_action"

        internal fun buildCreateIntent(
            context: Context,
            account: AccountModel,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            return Intent(context, GroupNameEditActivity::class.java)
                .putCommonExtras(account, callbackActivity, callbackAction)
        }

        internal fun buildEditIntent(
            context: Context,
            groupId: Long,
            groupName: String?,
            account: AccountModel,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            return Intent(context, GroupNameEditActivity::class.java)
                .putExtra(EXTRA_GROUP_ID, groupId)
                .putExtra(EXTRA_GROUP_NAME, groupName)
                .putCommonExtras(account, callbackActivity, callbackAction)
        }

        private fun Intent.putCommonExtras(
            account: AccountModel,
            callbackActivity: Class<out Activity>,
            callbackAction: String? = null,
        ): Intent {
            return putExtra(EXTRA_ACCOUNT, account)
                .putExtra(EXTRA_CALLBACK_ACTIVITY, callbackActivity)
                .putExtra(EXTRA_CALLBACK_ACTION, callbackAction)
        }
    }
}
