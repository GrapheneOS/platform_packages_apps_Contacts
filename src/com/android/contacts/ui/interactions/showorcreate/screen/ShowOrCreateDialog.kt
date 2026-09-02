package com.android.contacts.ui.interactions.showorcreate.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateAction as Action
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateUiState as State

@Composable
internal fun ShowOrCreateDialog(
    effectHandler: ShowOrCreateEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: ShowOrCreateScreenModel = viewModel<ShowOrCreateViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    ShowOrCreateDialogContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun ShowOrCreateDialogContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        State.Searching -> {
            // No need to show anything
        }
        is State.ConfirmingCreate -> {
            AlertDialog(
                onDismissRequest = { onAction(Action.CreateDismiss) },
                text = {
                    Text(
                        stringResource(
                            R.string.add_contact_dlg_message_fmt,
                            uiState.description.orEmpty(),
                        ),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onAction(Action.CreateConfirm) }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(Action.CreateDismiss) }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                modifier = modifier,
            )
        }
    }
}
