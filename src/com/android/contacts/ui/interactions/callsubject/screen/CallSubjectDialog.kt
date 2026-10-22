@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.interactions.callsubject.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.common.components.ContactAvatar
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.common.components.contactAvatarColorSeed
import com.android.contacts.ui.common.components.contactAvatarLabel
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_CALL_BUTTON
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_HISTORY_BUTTON
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_TEXT_FIELD
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectAction as Action
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectUiState as State
import com.android.contacts.util.core.extension.limit
import java.nio.charset.Charset
import kotlinx.collections.immutable.ImmutableList

private val DIALOG_PADDING_HORIZONTAL = 16.dp
private val DIALOG_PADDING_TOP = 16.dp
private val DIALOG_PADDING_BOTTOM = 8.dp
private val CONTACT_AVATAR_SIZE = 48.dp

@Composable
internal fun CallSubjectDialog(
    effectHandler: CallSubjectEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: CallSubjectScreenModel = viewModel<CallSubjectViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    CallSubjectContent(
        state = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun CallSubjectContent(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHistory by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = { onAction(Action.Dismiss) },
        content = {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = DIALOG_PADDING_HORIZONTAL)
                        .padding(top = DIALOG_PADDING_TOP, bottom = DIALOG_PADDING_BOTTOM),
                ) {
                    DialogHeader(state)

                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = showHistory,
                        onExpandedChange = {},
                    ) {
                        SubjectTextField(
                            subject = state.subject,
                            subjectSize = state.subjectSize,
                            subjectLimit = state.subjectLimit,
                            subjectEncoding = state.subjectEncoding,
                            onSubjectChange = {
                                showHistory = false
                                onAction(Action.SubjectChange(it))
                            },
                            isHistoryAvailable = state.isHistoryAvailable,
                            onHistoryClick = { showHistory = !showHistory },
                        )
                        HistoryMenu(
                            expanded = showHistory,
                            onDismiss = { showHistory = false },
                            history = state.history,
                            onItemClick = {
                                showHistory = false
                                onAction(Action.HistorySubjectPick(it))
                            },
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    CallButton(
                        onClick = { onAction(Action.CallClick) },
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.testTag(CALL_SUBJECT_CALL_BUTTON),
    ) {
        Text(stringResource(R.string.send_and_call_button))
    }
}

@Composable
private fun DialogHeader(state: State) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ContactAvatar(
            avatarImage = state.avatarImage,
            name = state.name,
            isBusiness = state.isBusiness,
        )

        Spacer(Modifier.width(8.dp))

        ContactDetails(
            name = state.name,
            number = state.number,
            numberLabel = state.numberLabel,
        )
    }
}

@Composable
private fun ContactAvatar(
    avatarImage: ContactAvatarImage?,
    name: String,
    isBusiness: Boolean,
) {
    ContactAvatar(
        avatarImage = avatarImage,
        size = CONTACT_AVATAR_SIZE,
        fallbackLabel = contactAvatarLabel(name),
        colorSeed = contactAvatarColorSeed(name),
        fallbackIcon = when {
            isBusiness -> Icons.Rounded.Business
            else -> Icons.Rounded.Person
        },
    )
}

@Composable
private fun ContactDetails(
    name: String,
    numberLabel: String?,
    number: String?,
) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!numberLabel.isNullOrEmpty() && !number.isNullOrEmpty()) {
            Text(
                text = stringResource(
                    R.string.call_subject_type_and_number,
                    numberLabel,
                    number,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExposedDropdownMenuBoxScope.SubjectTextField(
    subject: String,
    subjectSize: Int,
    subjectLimit: Int,
    subjectEncoding: Charset?,
    onSubjectChange: (String) -> Unit,
    isHistoryAvailable: Boolean,
    onHistoryClick: () -> Unit,
) {
    OutlinedTextField(
        value = subject,
        onValueChange = { onSubjectChange(it.limit(subjectLimit, subjectEncoding)) },
        label = { Text(stringResource(R.string.call_with_a_note)) },
        placeholder = { Text(stringResource(R.string.call_subject_hint)) },
        supportingText = {
            Text(
                text = stringResource(
                    R.string.call_subject_limit,
                    subjectSize,
                    subjectLimit,
                ),
                color = when {
                    subjectSize == subjectLimit -> MaterialTheme.colorScheme.error
                    else -> Color.Unspecified
                },
            )
        },
        trailingIcon = {
            if (isHistoryAvailable) {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.testTag(CALL_SUBJECT_HISTORY_BUTTON),
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.call_subject_history),
                    )
                }
            }
        },
        modifier = Modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
            .testTag(CALL_SUBJECT_TEXT_FIELD),
    )
}

@Composable
private fun ExposedDropdownMenuBoxScope.HistoryMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    history: ImmutableList<String>,
    onItemClick: (String) -> Unit,
) {
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        history.forEach { item ->
            DropdownMenuItem(
                text = { Text(item) },
                onClick = { onItemClick(item) },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CallSubjectDialogPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            CallSubjectContent(
                state = State(
                    avatarImage = null,
                    isBusiness = false,
                    name = "John Smith",
                    number = "+1 234-567-8901",
                    numberLabel = "Work",
                ),
                onAction = {},
            )
        }
    }
}
