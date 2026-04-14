package com.android.contacts.ui.contactcreation

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Intents.Insert
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.android.contacts.ContactSaveService
import com.android.contacts.activities.ContactEditorActivity.ContactEditor.SaveMode
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
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
            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
            ) { uri ->
                uri?.let { viewModel.onAction(ContactCreationAction.SetPhoto(it)) }
            }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture(),
            ) { success ->
                val uri = viewModel.pendingCameraUri
                viewModel.pendingCameraUri = null
                if (success && uri != null) {
                    viewModel.onAction(ContactCreationAction.SetPhoto(uri))
                }
            }

            EffectCollector(galleryLauncher, cameraLauncher)

            AppTheme {
                val uiState by viewModel.uiState.collectAsState()
                ContactCreationEditorScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                )
            }
        }
    }

    @Composable
    private fun EffectCollector(
        galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
        cameraLauncher: ActivityResultLauncher<android.net.Uri>,
    ) {
        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                handleEffect(effect, galleryLauncher, cameraLauncher)
            }
        }
    }

    private fun handleEffect(
        effect: ContactCreationEffect,
        galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
        cameraLauncher: ActivityResultLauncher<android.net.Uri>,
    ) {
        when (effect) {
            is ContactCreationEffect.Save -> {
                val saveIntent = ContactSaveService.createSaveContactIntent(
                    this,
                    effect.result.state,
                    ContactCreationViewModel.SAVE_MODE_EXTRA_KEY,
                    SaveMode.CLOSE,
                    false,
                    ContactCreationActivity::class.java,
                    ContactCreationViewModel.SAVE_COMPLETED_ACTION,
                    effect.result.updatedPhotos,
                    null,
                    null,
                )
                startService(saveIntent)
            }

            is ContactCreationEffect.NavigateBack -> finish()
            is ContactCreationEffect.SaveSuccess -> finish()

            is ContactCreationEffect.ShowError -> {
                Toast.makeText(this, getString(effect.messageResId), Toast.LENGTH_SHORT).show()
            }

            is ContactCreationEffect.LaunchGallery -> {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }

            is ContactCreationEffect.LaunchCamera -> cameraLauncher.launch(effect.outputUri)

            is ContactCreationEffect.LaunchAccountPicker -> {
                // Phase 2: show account picker bottom sheet or dialog
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ContactCreationViewModel.SAVE_COMPLETED_ACTION) {
            val contactUri = intent.data
            // Validate the callback URI has the expected contacts authority
            val isValidUri = contactUri == null ||
                contactUri.authority == android.provider.ContactsContract.AUTHORITY
            if (isValidUri) {
                viewModel.onSaveResult(contactUri != null, contactUri)
            }
        }
    }

    private fun applyIntentExtras(extras: SanitizedExtras) {
        extras.name?.let {
            viewModel.onAction(ContactCreationAction.UpdateFirstName(it))
        }
        extras.phone?.let {
            viewModel.onAction(
                ContactCreationAction.UpdatePhone(
                    id = viewModel.uiState.value.phoneNumbers.first().id,
                    value = it,
                ),
            )
        }
        extras.email?.let {
            viewModel.onAction(
                ContactCreationAction.UpdateEmail(
                    id = viewModel.uiState.value.emails.first().id,
                    value = it,
                ),
            )
        }
    }

    // Intentionally accepting only NAME, PHONE, EMAIL extras.
    // Insert.PHONE_TYPE, Insert.SECONDARY_PHONE, Insert.COMPANY, Insert.NOTES,
    // Insert.DATA (arbitrary ContentValues), and all other extras are ignored
    // for minimum attack surface on GrapheneOS.
    private fun sanitizeExtras(intent: Intent) = SanitizedExtras(
        name = intent.getStringExtra(Insert.NAME)?.take(MAX_NAME_LEN),
        phone = intent.getStringExtra(Insert.PHONE)?.take(MAX_PHONE_LEN),
        email = intent.getStringExtra(Insert.EMAIL)?.take(MAX_EMAIL_LEN),
    )

    private data class SanitizedExtras(val name: String?, val phone: String?, val email: String?)
}

private const val MAX_NAME_LEN = 500
private const val MAX_PHONE_LEN = 100
private const val MAX_EMAIL_LEN = 320
