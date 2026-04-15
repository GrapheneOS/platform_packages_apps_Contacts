package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

/**
 * Standalone single-field composables previously housed inside the "More Fields" section.
 * Now used directly by the editor screen with individual show/hide visibility.
 */

@Composable
internal fun NicknameField(
    nickname: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow {
        OutlinedTextField(
            value = nickname,
            onValueChange = { onAction(ContactCreationAction.UpdateNickname(it)) },
            label = { Text(stringResource(R.string.nicknameLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NICKNAME_FIELD),
            singleLine = true,
        )
    }
}

@Composable
internal fun NoteField(
    note: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow {
        OutlinedTextField(
            value = note,
            onValueChange = { onAction(ContactCreationAction.UpdateNote(it)) },
            label = { Text(stringResource(R.string.contact_creation_note)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NOTE_FIELD),
            singleLine = false,
            maxLines = 4,
        )
    }
}

@Composable
internal fun SipField(
    sipAddress: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow {
        OutlinedTextField(
            value = sipAddress,
            onValueChange = { onAction(ContactCreationAction.UpdateSipAddress(it)) },
            label = { Text(stringResource(R.string.contact_creation_sip)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.SIP_FIELD),
            singleLine = true,
        )
    }
}
