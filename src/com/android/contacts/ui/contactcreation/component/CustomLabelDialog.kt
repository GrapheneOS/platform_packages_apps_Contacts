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
import com.android.contacts.ui.contactcreation.TestTags

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
        modifier = Modifier.testTag(TestTags.CUSTOM_LABEL_DIALOG),
        title = { Text(stringResource(R.string.contact_creation_custom_label_title)) },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(stringResource(R.string.contact_creation_custom_label_hint)) },
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .testTag(TestTags.CUSTOM_LABEL_INPUT),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label) },
                enabled = label.isNotBlank(),
                modifier = Modifier.testTag(TestTags.CUSTOM_LABEL_OK),
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.CUSTOM_LABEL_CANCEL),
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
