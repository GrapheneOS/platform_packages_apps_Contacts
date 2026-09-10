package com.android.contacts.ui.editor.springboard.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.contacts.R
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_PICK_RAW_CONTACT_TEST_TAG
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickRawContactToEditDialog(
    contacts: ImmutableList<RawContactUiModel>,
    onDismiss: () -> Unit,
    onContactPicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.contact_editor_pick_raw_contact_to_edit_dialog_title))
        },
        text = {
            RawContactsSelectionList(
                contacts = contacts,
                onContactPicked = onContactPicked,
            )
        },
        dismissButton = null,
        confirmButton = {},
        modifier = modifier
            .testTag(CONTACT_EDITOR_SB_PICK_RAW_CONTACT_TEST_TAG),
    )
}
