package com.android.contacts.ui.contactdetails

import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.QuickContact
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import com.android.contacts.R
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.activities.RequestPermissionsActivity
import com.android.contacts.editor.EditorUiUtils
import com.android.contacts.interactions.ContactDeletionInteraction
import com.android.contacts.ui.contactdetails.screen.ContactDetailsEffectHandlerImpl
import com.android.contacts.ui.contactdetails.screen.ContactDetailsScreen
import com.android.contacts.ui.contactdetails.screen.ContactDetailsViewModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.core.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactDetailsActivity : ComponentActivity() {

    @Inject
    internal lateinit var clipboardManager: ClipboardManager

    private val viewModel by viewModels<ContactDetailsViewModel>()

    private val editorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::applyEditorResult,
    )

    private val directoryCopyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::applyDirectoryCopyResult,
    )

    private val joinTargetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::applyJoinTargetResult,
    )

    private val ringtoneLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::applyRingtoneResult,
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
            joinTargetLauncher = joinTargetLauncher,
            ringtoneLauncher = ringtoneLauncher,
            editorLauncher = editorLauncher,
            directoryCopyLauncher = directoryCopyLauncher,
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
        if (intent.action == ACTION_SPLIT_COMPLETED) {
            Toast.makeText(this, R.string.contactUnlinkedToast, Toast.LENGTH_SHORT).show()
            finish()
            return false
        }

        if (intent.getBooleanExtra(EXTRA_CONTACT_EDITED, false)) {
            setResult(ContactEditorActivity.RESULT_CODE_EDITED)
        }

        val lookupUri = intent.data
        if (lookupUri == null) {
            finish()
            return false
        }

        viewModel.bind(
            lookupUri = lookupUri,
            excludedMimeTypes = excludedMimeTypes(intent),
            prioritizedMimeType = intent.getStringExtra(QuickContact.EXTRA_PRIORITIZED_MIMETYPE),
            callbackActivity = ContactDetailsActivity::class.java,
        )

        return true
    }

    private fun excludedMimeTypes(intent: Intent): Set<String> {
        return intent.getStringArrayExtra(QuickContact.EXTRA_EXCLUDE_MIMES)
            .orEmpty()
            .toSet()
    }

    private fun applyEditorResult(result: ActivityResult) {
        setResult(result.resultCode)

        val isDeletedOrSplit = result.resultCode == ContactDeletionInteraction.RESULT_CODE_DELETED ||
            result.resultCode == ContactEditorActivity.RESULT_CODE_SPLIT

        if (isDeletedOrSplit) {
            finish()
        }
    }

    private fun applyDirectoryCopyResult(result: ActivityResult) {
        setResult(result.resultCode)

        val data = result.data
        if (result.resultCode == RESULT_CANCELED || data == null) {
            return
        }

        bindIntent(data)
    }

    private fun applyJoinTargetResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            return
        }

        val contactUri = result.data?.data ?: return

        viewModel.onAction(Action.JoinTargetPicked(ContentUris.parseId(contactUri)))
    }

    private fun applyRingtoneResult(result: ActivityResult) {
        val data = result.data ?: return
        val pickedUri = IntentCompat.getParcelableExtra(
            data,
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            Uri::class.java,
        )

        viewModel.onAction(
            Action.RingtonePicked(
                EditorUiUtils.getRingtoneStringFromUri(pickedUri, Build.VERSION.SDK_INT),
            ),
        )
    }

    companion object {
        const val ACTION_SPLIT_COMPLETED: String = "splitCompleted"
        const val EXTRA_CONTACT_EDITED: String = "contact_edited"
        const val EXTRA_PREVIOUS_SCREEN_TYPE: String = "previous_screen_type"
    }
}
