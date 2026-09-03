package com.android.contacts.ui.contactdetails

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.ui.contactdetails.screen.ContactDetailsEffectHandlerImpl
import com.android.contacts.ui.contactdetails.screen.ContactDetailsScreen
import com.android.contacts.ui.contactdetails.screen.ContactDetailsViewModel
import com.android.contacts.ui.core.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactDetailsActivity : ComponentActivity() {

    @Inject
    internal lateinit var clipboardManager: ClipboardManager

    @Inject
    internal lateinit var contactEntryIntentFactory: ContactEntryIntentFactory

    private val viewModel by viewModels<ContactDetailsViewModel>()

    private val launchers = ContactDetailsLaunchers(
        editor = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::applyEditorResult,
        ),
        directoryCopy = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::applyDirectoryCopyResult,
        ),
        joinTarget = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::applyJoinTargetResult,
        ),
        ringtone = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::applyRingtoneResult,
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RequestPermissionsActivity.startPermissionActivityIfNeeded(this)) {
            return
        }

        enableEdgeToEdge()

        if (!bindIntent(intent)) {
            return
        }

        val effectHandler = ContactDetailsEffectHandlerImpl(
            activity = this,
            clipboardManager = clipboardManager,
            contactEntryIntentFactory = contactEntryIntentFactory,
            launchers = launchers,
        )

        setContent {
            AppTheme {
                ContactDetailsScreen(
                    onEffect = effectHandler::handle,
                    onNavigateBack = ::finish,
                    screenModel = viewModel,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bindIntent(intent)
    }

    private fun bindIntent(intent: Intent): Boolean {
        if (intent.isContactEdited()) {
            setResult(ContactEditorActivity.RESULT_CODE_EDITED)
        }

        val arguments = intent.toContactDetailsArguments()
        if (arguments == null) {
            finish()
            return false
        }

        viewModel.bind(
            arguments = arguments,
            callbackActivity = ContactDetailsActivity::class.java,
        )

        return true
    }

    private fun applyEditorResult(result: ActivityResult) {
        setResult(result.resultCode)

        if (result.isContactGone()) {
            finish()
        }
    }

    private fun applyDirectoryCopyResult(result: ActivityResult) {
        setResult(result.resultCode)

        val intent = result.toRebindIntent() ?: return

        setIntent(intent)
        bindIntent(intent)
    }

    private fun applyJoinTargetResult(result: ActivityResult) {
        val action = result.toJoinTargetAction() ?: return

        viewModel.onAction(action)
    }

    private fun applyRingtoneResult(result: ActivityResult) {
        val action = result.toRingtoneAction() ?: return

        viewModel.onAction(action)
    }

    companion object {
        const val EXTRA_CONTACT_EDITED: String = "contact_edited"
    }
}
