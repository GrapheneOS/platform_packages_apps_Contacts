package com.android.contacts.ui.interactions.showorcreate

import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateDialog
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateEffectHandlerImpl
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowOrCreateActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        val effectHandler = ShowOrCreateEffectHandlerImpl(
            activity = this,
            intentExtras = intent.extras,
        )

        setContent {
            AppTheme {
                ShowOrCreateDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    override val defaultViewModelCreationExtras: CreationExtras
        get() {
            return CreationExtras(super.defaultViewModelCreationExtras) {
                set(
                    DEFAULT_ARGS_KEY,
                    Bundle().apply {
                        putParcelable(
                            ShowOrCreateViewModel.EXTRA_DATA,
                            intent.data,
                        )
                        putString(
                            ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION,
                            intent.getStringExtra(
                                ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION
                            ),
                        )
                        putBoolean(
                            ContactsContract.Intents.EXTRA_FORCE_CREATE,
                            intent.getBooleanExtra(
                                ContactsContract.Intents.EXTRA_FORCE_CREATE,
                                false,
                            ),
                        )
                    },
                )
            }
        }
}
