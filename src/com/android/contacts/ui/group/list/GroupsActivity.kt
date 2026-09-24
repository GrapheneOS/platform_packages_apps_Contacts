package com.android.contacts.ui.group.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.group.list.screen.GroupsEffectHandler
import com.android.contacts.ui.group.list.screen.GroupsEffectHandlerImpl
import com.android.contacts.ui.group.list.screen.GroupsScreen
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupsActivity : ComponentActivity() {

    private var effectHandler: GroupsEffectHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = GroupsEffectHandlerImpl(
            activity = this,
        )
        this.effectHandler = effectHandler

        setContent {
            AppTheme {
                GroupsScreen(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == GroupUtil.ACTION_CREATE_GROUP) {
            intent.data?.let { groupUri ->
                effectHandler?.handle(Effect.OpenGroup(groupUri))
            }
        }
    }

    companion object {
        const val EXTRA_ACCOUNT = "account"
        const val REQUEST_CREATE_GROUP = 1001

        internal fun buildIntent(
            context: Context,
            account: AccountModel? = null,
        ): Intent {
            return Intent(context, GroupsActivity::class.java)
                .putExtra(EXTRA_ACCOUNT, account)
        }
    }
}
