package com.android.contacts.ui.editor.springboard.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.ui.core.ContactsPreviewDialog
import com.android.contacts.ui.editor.springboard.common.LinkedContactsDialog
import com.android.contacts.ui.editor.springboard.common.PickRawContactToEditDialog
import com.android.contacts.ui.editor.springboard.common.SplitContactConfirmationDialog
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardAction as Action
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardUiState as State
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContactEditorSpringBoardScreen(
    effectHandler: ContactEditorSpringBoardEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: ContactEditorSpringBoardScreenModel =
        viewModel<ContactEditorSpringBoardViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    ContactEditorSpringBoardContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun ContactEditorSpringBoardContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        State.Loading -> {
            // Show nothing because the user could be going straight to the contact editor
        }
        is State.ShowPickContactToEdit -> {
            PickRawContactToEditDialog(
                contacts = uiState.contacts,
                onDismiss = { onAction(Action.DialogDismissed) },
                onContactPicked = { onAction(Action.ContactClicked(it)) },
                modifier = modifier,
            )
        }
        is State.ShowLinkedContacts -> {
            LinkedContactsDialog(
                contacts = uiState.contacts,
                isUserProfile = uiState.isUserProfile,
                onDismiss = { onAction(Action.DialogDismissed) },
                onContactClick = { onAction(Action.ContactClicked(it)) },
                onAdd = { onAction(Action.AddClicked) },
                onUnlink = { onAction(Action.UnlinkClicked) },
                modifier = modifier,
            )
        }
        State.ShowUnlinkConfirmation -> {
            SplitContactConfirmationDialog(
                hasPendingChanges = false,
                onConfirm = { onAction(Action.UnlinkConfirmed) },
                onDismiss = { onAction(Action.UnlinkDismissed) },
                modifier = modifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactEditorSpringBoardPickContactPreview() {
    ContactsPreviewDialog {
        ContactEditorSpringBoardContent(
            uiState = State.ShowPickContactToEdit(
                contacts = persistentListOf(buildRawContactForPreview()),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactEditorSpringBoardLinkedContactsPreview() {
    ContactsPreviewDialog {
        ContactEditorSpringBoardContent(
            uiState = State.ShowLinkedContacts(
                contacts = persistentListOf(buildRawContactForPreview()),
                isUserProfile = false,
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactEditorSpringBoardUnlinkConfirmationPreview() {
    ContactsPreviewDialog {
        ContactEditorSpringBoardContent(
            uiState = State.ShowUnlinkConfirmation,
            onAction = {},
        )
    }
}

private fun buildRawContactForPreview(): RawContactUiModel {
    return RawContactUiModel(
        id = 1L,
        avatarImage = null,
        displayName = "John Smith",
        accountLabel = "Device",
        accountIconData = null,
    )
}
