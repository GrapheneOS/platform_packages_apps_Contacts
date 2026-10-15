package com.android.contacts.ui.vcard.cancel.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewDialog
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_CONFIRM_BUTTON_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_CONFIRM_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_DISMISS_BUTTON_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_ERROR_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportAction as Action
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportUiState as State
import com.android.contacts.ui.vcard.cancel.screen.model.VCardOperationType

@Composable
internal fun CancelImportExportDialog(
    effectHandler: CancelImportExportEffectHandler,
    screenModel: CancelImportExportScreenModel = viewModel<CancelImportExportViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    CancelImportExportContent(
        uiState = uiState,
        onAction = screenModel::onAction,
    )
}

@Composable
internal fun CancelImportExportContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = { onAction(Action.DISMISS) },
            icon = cancelImportExportIcon(uiState),
            text = { CancelImportExportText(uiState) },
            confirmButton = { CancelConfirmButton(uiState, onAction) },
            dismissButton = cancelDismissButton(uiState, onAction),
            modifier = modifier
                .testTag(
                    when (uiState) {
                        is State.Confirming -> CANCEL_CONFIRM_DIALOG_TEST_TAG
                        State.Error -> CANCEL_ERROR_DIALOG_TEST_TAG
                    },
                ),
        )
    }
}

@Composable
private fun cancelImportExportIcon(uiState: State): @Composable (() -> Unit)? {
    return when (uiState) {
        is State.Confirming -> null
        State.Error -> {
            {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun CancelImportExportText(uiState: State) {
    Text(
        when (uiState) {
            is State.Confirming -> stringResource(
                when (uiState.type) {
                    VCardOperationType.IMPORT ->
                        R.string.cancel_import_confirmation_message
                    VCardOperationType.EXPORT ->
                        R.string.cancel_export_confirmation_message
                },
                uiState.displayName,
            )
            State.Error -> stringResource(R.string.cancel_vcard_import_or_export_failed)
        },
    )
}

@Composable
private fun CancelConfirmButton(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    val action = when (uiState) {
        is State.Confirming -> Action.CONFIRM
        State.Error -> Action.DISMISS
    }
    val textResource = when (uiState) {
        is State.Confirming -> R.string.yes_button
        State.Error -> android.R.string.ok
    }
    TextButton(
        onClick = { onAction(action) },
        modifier = Modifier.testTag(CANCEL_CONFIRM_BUTTON_TEST_TAG),
    ) {
        Text(stringResource(textResource))
    }
}

@Composable
private fun cancelDismissButton(
    uiState: State,
    onAction: (Action) -> Unit,
): @Composable (() -> Unit)? {
    return when (uiState) {
        is State.Confirming -> {
            {
                TextButton(
                    onClick = { onAction(Action.DISMISS) },
                    modifier = Modifier.testTag(CANCEL_DISMISS_BUTTON_TEST_TAG),
                ) {
                    Text(stringResource(R.string.no_button))
                }
            }
        }
        State.Error -> null
    }
}

@PreviewLightDark
@Composable
private fun ShowOrCreateDialogImportPreview() {
    ContactsPreviewDialog {
        CancelImportExportContent(
            uiState = State.Confirming(
                type = VCardOperationType.IMPORT,
                displayName = "contacts.vcf",
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ShowOrCreateDialogExportPreview() {
    ContactsPreviewDialog {
        CancelImportExportContent(
            uiState = State.Confirming(
                type = VCardOperationType.EXPORT,
                displayName = "contacts.vcf",
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ShowOrCreateDialogErrorPreview() {
    ContactsPreviewDialog {
        CancelImportExportContent(
            uiState = State.Error,
            onAction = {},
        )
    }
}
