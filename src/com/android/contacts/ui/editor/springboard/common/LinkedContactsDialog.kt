package com.android.contacts.ui.editor.springboard.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.contacts.R
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_ADD_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_LINKED_CONTACTS_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_UNLINK_BUTTON_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LinkedContactsDialog(
    contacts: ImmutableList<RawContactUiModel>,
    isUserProfile: Boolean,
    onDismiss: () -> Unit,
    onContactClick: (Long) -> Unit,
    onUnlink: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.contact_editor_pick_linked_contact_dialog_title))
        },
        text = {
            RawContactsSelectionList(
                contacts = contacts,
                onContactPicked = onContactClick,
            )
        },
        dismissButton = {
            if (!isUserProfile) {
                TextButton(
                    onClick = onUnlink,
                    modifier = Modifier.testTag(CONTACT_EDITOR_SB_UNLINK_BUTTON_TEST_TAG),
                ) {
                    Text(stringResource(R.string.contact_editor_unlink_contacts))
                }
            }
        },
        confirmButton = {
            if (!isUserProfile) {
                TextButton(
                    onClick = onAdd,
                    modifier = Modifier.testTag(CONTACT_EDITOR_SB_ADD_BUTTON_TEST_TAG),
                ) {
                    Text(stringResource(R.string.contact_editor_add_linked_contact))
                }
            }
        },
        modifier = modifier
            .testTag(CONTACT_EDITOR_SB_LINKED_CONTACTS_TEST_TAG),
    )
}
