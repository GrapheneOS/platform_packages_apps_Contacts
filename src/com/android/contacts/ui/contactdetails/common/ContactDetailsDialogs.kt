package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG
import com.android.contacts.ui.core.ContactsPreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactDetailsProgressDialog(
    operation: ContactLinkOperation,
    modifier: Modifier = Modifier,
) {
    val message = when (operation) {
        ContactLinkOperation.LINK -> R.string.contacts_linking_progress_bar
        ContactLinkOperation.UNLINK -> R.string.contacts_unlinking_progress_bar
    }

    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        modifier = modifier.testTag(CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG),
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Tokens.progressDialogSpacing),
                modifier = Modifier.padding(all = Tokens.progressDialogPadding),
            ) {
                CircularProgressIndicator()

                Text(
                    text = stringResource(message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AlertDialogDefaults.textContentColor,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsProgressDialogPreview() {
    ContactsPreviewTheme {
        ContactDetailsProgressDialog(operation = ContactLinkOperation.LINK)
    }
}
