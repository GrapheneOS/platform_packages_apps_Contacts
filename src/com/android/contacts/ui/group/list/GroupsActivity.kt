package com.android.contacts.ui.group.list

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Intents.Insert
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.ContactSaveService
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.group.list.screen.GroupsEffectHandler
import com.android.contacts.ui.group.list.screen.GroupsEffectHandlerImpl
import com.android.contacts.ui.group.list.screen.GroupsScreen
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupsActivity :
    ComponentActivity(),
    ContactSaveService.Listener {

    private var effectHandler: GroupsEffectHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        enableEdgeToEdge()
        ContactSaveService.registerListener(this)

        val effectHandler = GroupsEffectHandlerImpl(
            activity = this,
        )
        this.effectHandler = effectHandler

        if (savedInstanceState == null && intent.action == Intent.ACTION_INSERT) {
            insertAccount()?.let { effectHandler.handle(Effect.CreateNewGroup(it)) }
        }

        setContent {
            AppTheme {
                GroupsScreen(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    override fun onDestroy() {
        ContactSaveService.unregisterListener(this)
        super.onDestroy()
    }

    override fun onServiceCompleted(callbackIntent: Intent?) {
        val intent = callbackIntent ?: return
        if (intent.action == GroupUtil.ACTION_CREATE_GROUP) {
            intent.data?.let { groupUri ->
                effectHandler?.handle(Effect.OpenGroup(groupUri))
            }
        }
    }

    private fun insertAccount(): AccountModel? {
        val account = intent.getParcelableExtra(Insert.EXTRA_ACCOUNT, Account::class.java)
            ?: return null
        return AccountModel(
            name = account.name,
            type = account.type,
            dataSet = intent.getStringExtra(Insert.EXTRA_DATA_SET),
        )
    }

    companion object {
        const val EXTRA_ACCOUNT = "account"

        @JvmStatic
        internal fun buildIntent(
            context: Context,
            account: AccountModel? = null,
        ): Intent {
            return Intent(context, GroupsActivity::class.java)
                .putExtra(EXTRA_ACCOUNT, account)
        }
    }
}
