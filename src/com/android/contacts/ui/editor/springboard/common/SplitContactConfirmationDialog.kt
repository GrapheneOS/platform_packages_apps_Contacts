package com.android.contacts.ui.editor.springboard.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.contacts.R
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CANCEL_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CONFIRMATION_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_SPLIT_CONFIRM_BUTTON_TEST_TAG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SplitContactConfirmationDialog(
    hasPendingChanges: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = stringResource(
                    when {
                        hasPendingChanges -> R.string.splitConfirmationWithPendingChanges
                        else -> R.string.splitConfirmation
                    },
                ),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(CONTACT_EDITOR_SB_SPLIT_CANCEL_BUTTON_TEST_TAG),
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(CONTACT_EDITOR_SB_SPLIT_CONFIRM_BUTTON_TEST_TAG),
            ) {
                Text(
                    stringResource(
                        when {
                            hasPendingChanges ->
                                R.string.splitConfirmationWithPendingChanges_positive_button
                            else ->
                                R.string.splitConfirmation_positive_button
                        },
                    ),
                )
            }
        },
        modifier = modifier
            .testTag(CONTACT_EDITOR_SB_SPLIT_CONFIRMATION_TEST_TAG),
    )
}
