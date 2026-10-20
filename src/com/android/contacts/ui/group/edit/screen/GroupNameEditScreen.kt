package com.android.contacts.ui.group.edit.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.domain.groups.model.GROUP_NAME_MAX_LENGTH
import com.android.contacts.ui.core.ContactsPreviewDialog
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_CANCEL_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_DIALOG_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_DIALOG_TITLE_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_FIELD_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_OK_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State

@Composable
internal fun GroupNameEditScreen(
    effectHandler: GroupNameEditEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: GroupNameEditScreenModel = viewModel<GroupNameEditViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    GroupNameEditContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun GroupNameEditContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { onAction(Action.Dismissed) },
        dismissButton = {
            TextButton(
                onClick = { onAction(Action.Dismissed) },
                modifier = Modifier.testTag(GROUP_NAME_EDIT_CANCEL_TEST_TAG),
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(Action.OkClicked) },
                enabled = uiState.isSaveEnabled,
                modifier = Modifier.testTag(GROUP_NAME_EDIT_OK_TEST_TAG),
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = {
            Text(
                text = stringResource(
                    when {
                        uiState.isEdit -> R.string.group_name_dialog_update_title
                        else -> R.string.group_name_dialog_insert_title
                    },
                ),
                modifier = Modifier.testTag(GROUP_NAME_EDIT_DIALOG_TITLE_TEST_TAG),
            )
        },
        text = {
            GroupNameEditField(
                name = uiState.name,
                isDuplicatedName = uiState.isDuplicatedName,
                onNameChange = { onAction(Action.NameChanged(it)) },
            )
        },
        modifier = modifier.testTag(GROUP_NAME_EDIT_DIALOG_TEST_TAG),
    )
}

@Composable
private fun GroupNameEditField(
    name: String,
    isDuplicatedName: Boolean,
    onNameChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = name,
        onValueChange = { onNameChange(it.take(GROUP_NAME_MAX_LENGTH)) },
        label = { Text(stringResource(R.string.group_name_dialog_hint)) },
        singleLine = true,
        isError = isDuplicatedName,
        supportingText = {
            if (isDuplicatedName) {
                Text(stringResource(R.string.groupExistsErrorMessage))
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .testTag(GROUP_NAME_EDIT_FIELD_TEST_TAG),
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@PreviewLightDark
@Composable
private fun GroupNameEditScreenCreatePreview() {
    ContactsPreviewDialog {
        GroupNameEditContent(
            uiState = State(
                isEdit = false,
                name = "Group Name",
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GroupNameEditScreenEditWithErrorPreview() {
    ContactsPreviewDialog {
        GroupNameEditContent(
            uiState = State(
                isEdit = true,
                name = "Group Name",
                isDuplicatedName = true,
            ),
            onAction = {},
        )
    }
}
