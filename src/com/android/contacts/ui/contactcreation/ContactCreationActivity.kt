package com.android.contacts.ui.contactcreation

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Intents.Insert
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.android.contacts.ui.core.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class ContactCreationActivity : ComponentActivity() {

    private val viewModel: ContactCreationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            val extras = sanitizeExtras(intent)
            applyIntentExtras(extras)
        }

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsState()
                ContactCreationEditorScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ContactCreationViewModel.SAVE_COMPLETED_ACTION) {
            val success = intent.data != null
            viewModel.onSaveResult(success, intent.data)
        }
    }

    private fun applyIntentExtras(extras: SanitizedExtras) {
        extras.name?.let {
            viewModel.onAction(
                com.android.contacts.ui.contactcreation.model.ContactCreationAction.UpdateFirstName(
                    it
                ),
            )
        }
        extras.phone?.let {
            viewModel.onAction(
                com.android.contacts.ui.contactcreation.model.ContactCreationAction.UpdatePhone(
                    id = viewModel.uiState.value.phoneNumbers.first().id,
                    value = it,
                ),
            )
        }
        extras.email?.let {
            viewModel.onAction(
                com.android.contacts.ui.contactcreation.model.ContactCreationAction.UpdateEmail(
                    id = viewModel.uiState.value.emails.first().id,
                    value = it,
                ),
            )
        }
    }

    private fun sanitizeExtras(intent: Intent): SanitizedExtras {
        return SanitizedExtras(
            name = intent.getStringExtra(Insert.NAME)?.take(MAX_NAME_LEN),
            phone = intent.getStringExtra(Insert.PHONE)?.take(MAX_PHONE_LEN),
            email = intent.getStringExtra(Insert.EMAIL)?.take(MAX_EMAIL_LEN),
        )
    }

    private data class SanitizedExtras(val name: String?, val phone: String?, val email: String?)

    private companion object {
        const val MAX_NAME_LEN = 500
        const val MAX_PHONE_LEN = 100
        const val MAX_EMAIL_LEN = 320
    }
}
