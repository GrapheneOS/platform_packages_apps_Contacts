package com.android.contacts.ui.contactcreation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.contacts.R

internal const val CUSTOM_LABEL_DIALOG_TAG = "custom_label_dialog"
internal const val CUSTOM_LABEL_INPUT_TAG = "custom_label_input"
internal const val CUSTOM_LABEL_OK_TAG = "custom_label_ok"
internal const val CUSTOM_LABEL_CANCEL_TAG = "custom_label_cancel"

@Composable
internal fun CustomLabelDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var label by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(CUSTOM_LABEL_DIALOG_TAG),
        title = { Text(stringResource(R.string.contact_creation_custom_label_title)) },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(stringResource(R.string.contact_creation_custom_label_hint)) },
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .testTag(CUSTOM_LABEL_INPUT_TAG),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label) },
                enabled = label.isNotBlank(),
                modifier = Modifier.testTag(CUSTOM_LABEL_OK_TAG),
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(CUSTOM_LABEL_CANCEL_TAG),
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
