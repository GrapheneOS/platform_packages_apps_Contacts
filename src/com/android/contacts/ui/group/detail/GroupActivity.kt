package com.android.contacts.ui.group.detail

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Groups
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.ContactSaveService
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.group.detail.screen.GroupEffectHandlerImpl
import com.android.contacts.ui.group.detail.screen.GroupScreen
import com.android.contacts.ui.group.detail.screen.GroupViewModel
import com.android.contacts.ui.group.detail.screen.model.GroupAction
import com.android.contacts.ui.selection.ContactSelectionIntents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupActivity :
    ComponentActivity(),
    ContactSaveService.Listener {

    private val viewModel by viewModels<GroupViewModel>()

    private val pickMembersLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::onMembersPicked,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        enableEdgeToEdge()
        ContactSaveService.registerListener(this)

        val snackbarHostState = SnackbarHostState()
        val effectHandler = GroupEffectHandlerImpl(
            activity = this,
            pickMembersLauncher = pickMembersLauncher,
            snackbarHostState = snackbarHostState,
            coroutineScope = lifecycleScope,
        )

        setContent {
            AppTheme {
                GroupScreen(
                    effectHandler = effectHandler,
                    snackbarHostState = snackbarHostState,
                    screenModel = viewModel,
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
        viewModel.onAction(
            GroupAction.SaveCompleted(action = intent.action, isSuccessful = intent.data != null),
        )
    }

    private fun onMembersPicked(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        val contactIds = ContactSelectionIntents.getPickedGroupMemberIds(result.data)
        viewModel.onAction(GroupAction.MembersPicked(contactIds))
    }

    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            return CreationExtras(super.defaultViewModelCreationExtras) {
                set(
                    DEFAULT_ARGS_KEY,
                    Bundle().apply { putParcelable(EXTRA_GROUP_URI, intent.data) },
                )
            }
        }

    companion object {
        const val EXTRA_GROUP_URI = "group_uri"

        internal fun buildIntent(
            context: Context,
            groupId: Long,
        ): Intent {
            val groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId)
            return buildIntent(context, groupUri)
        }

        internal fun buildIntent(
            context: Context,
            groupUri: Uri,
        ): Intent {
            return Intent(context, GroupActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(groupUri)
        }
    }
}
