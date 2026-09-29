package com.android.contacts.ui.editor.springboard

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.editor.springboard.screen.ContactEditorSpringBoardEffectHandlerImpl
import com.android.contacts.ui.editor.springboard.screen.ContactEditorSpringBoardScreen
import com.android.contacts.ui.editor.springboard.screen.ContactEditorSpringBoardViewModel
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardAction as Action
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactEditorSpringBoardActivity : ComponentActivity() {

    private val viewModel by viewModels<ContactEditorSpringBoardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        if (Intent.ACTION_EDIT != intent.action) {
            finish()
            return
        }

        val contactSelectLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::onContactSelectResult,
        )
        val originalExtras = intent.extras?.deepCopy() ?: Bundle()
        val effectHandler = ContactEditorSpringBoardEffectHandlerImpl(
            activity = this,
            contactSelectLauncher = contactSelectLauncher,
            originalExtras = originalExtras,
        )

        setContent {
            AppTheme {
                ContactEditorSpringBoardScreen(
                    effectHandler = effectHandler,
                    screenModel = viewModel,
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
                        putParcelable(EXTRA_URI, intent.data)
                        putBoolean(
                            EXTRA_SHOW_READ_ONLY,
                            intent.getBooleanExtra(EXTRA_SHOW_READ_ONLY, false),
                        )
                    },
                )
            }
        }

    private fun onContactSelectResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        result.data?.data?.let { uri ->
            viewModel.onAction(Action.AddContactSelected(uri))
        }
    }

    companion object {
        const val EXTRA_URI = "uri"
        const val EXTRA_SHOW_READ_ONLY = "showReadOnly"
    }
}
