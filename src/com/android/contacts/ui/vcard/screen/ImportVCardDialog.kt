package com.android.contacts.ui.vcard.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_CANCEL_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcard.screen.model.ImportVCardEffect
import com.android.contacts.ui.vcard.screen.model.ImportVCardUiState as State
import com.android.contacts.vcard.VCardService

@Composable
internal fun ImportVCardDialog(
    effectHandler: ImportVCardEffectHandler,
    screenModel: ImportVCardScreenModel = viewModel<ImportVCardViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    ImportVCardDialogEffects(
        screenModel,
        effectHandler,
    )

    ImportVCardContent(
        uiState = uiState,
        onAction = screenModel::onAction,
    )
}

@Composable
private fun ImportVCardDialogEffects(
    screenModel: ImportVCardScreenModel,
    effectHandler: ImportVCardEffectHandler,
) {
    val effectHandler by rememberUpdatedState(effectHandler)
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { screenModel.onAction(Action.PermissionRequestFinished) }
    val selectAccountLauncher = rememberLauncherForActivityResult(
        contract = SelectAccountActivity.Contract(),
    ) { screenModel.onAction(Action.AccountSelected(it)) }
    val selectFilesLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenMultipleDocuments() {
            override fun createIntent(
                context: Context,
                input: Array<String>,
            ): Intent {
                return super.createIntent(context, input)
                    .addCategory(Intent.CATEGORY_OPENABLE)
            }
        },
    ) { screenModel.onAction(Action.FilesSelected(it)) }

    LaunchedEffect(screenModel) {
        screenModel.effects.collect { effect ->
            when (effect) {
                is ImportVCardEffect.RequestPermissions ->
                    permissionsLauncher.launch(effect.permissions.toTypedArray())

                is ImportVCardEffect.SelectAccount ->
                    selectAccountLauncher.launch(
                        SelectAccountActivity.Contract.Request(
                            titleResId = R.string.dialog_new_contact_account,
                            accountFilter = AccountTypeManager.AccountFilter.CONTACTS_INSERTABLE,
                        ),
                    )

                is ImportVCardEffect.SelectFiles ->
                    selectFilesLauncher.launch(arrayOf(VCardService.X_VCARD_MIME_TYPE))

                else ->
                    effectHandler.handle(effect)
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        screenModel.onResume()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportVCardContent(
    uiState: State,
    onAction: (Action) -> Unit = {},
) {
    when (uiState) {
        State.Preparing -> {}
        State.Importing,
        State.Cancelling,
        -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { onAction(Action.CancelClicked) },
                        enabled = uiState != State.Cancelling,
                        modifier = Modifier.testTag(IMPORT_VCARD_CANCEL_TEST_TAG),
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.caching_vcard_title)) },
                text = { Text(stringResource(R.string.caching_vcard_message)) },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
                modifier = Modifier.testTag(IMPORT_VCARD_DIALOG_TEST_TAG),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ImportVCardDialogImportingPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportVCardContent(uiState = State.Importing)
        }
    }
}

@PreviewLightDark
@Composable
private fun ImportVCardDialogCancellingPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportVCardContent(uiState = State.Cancelling)
        }
    }
}
