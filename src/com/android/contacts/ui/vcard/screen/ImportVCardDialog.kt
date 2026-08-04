package com.android.contacts.ui.vcard.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_CANCEL_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcard.screen.model.ImportVCardUiState as State

@Composable
internal fun ImportVCardDialog(
    effectHandler: ImportVCardEffectHandler,
    screenModel: ImportVCardScreenModel = viewModel<ImportVCardViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    ImportVCardEffects(
        screenModel = screenModel,
        effectHandler = effectHandler,
    )

    ImportVCardContent(
        uiState = uiState,
        onAction = screenModel::onAction,
    )

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
        State.PREPARING -> {}
        State.IMPORTING,
        State.CANCELLING,
        -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { onAction(Action.CancelClicked) },
                        enabled = uiState != State.CANCELLING,
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
            ImportVCardContent(uiState = State.IMPORTING)
        }
    }
}

@PreviewLightDark
@Composable
private fun ImportVCardDialogCancellingPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportVCardContent(uiState = State.CANCELLING)
        }
    }
}
