package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.NameState

/**
 * Name section as a @Composable for Column-based layout.
 * Uses FieldRow with Person icon on first field only.
 */
@Composable
internal fun NameSectionContent(
    nameState: NameState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FieldRow(icon = Icons.Filled.Person) {
            OutlinedTextField(
                value = nameState.first,
                onValueChange = { onAction(ContactCreationAction.UpdateFirstName(it)) },
                label = { Text(stringResource(R.string.contact_creation_first_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.NAME_FIRST),
                singleLine = true,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FieldRow(icon = null) {
            OutlinedTextField(
                value = nameState.last,
                onValueChange = { onAction(ContactCreationAction.UpdateLastName(it)) },
                label = { Text(stringResource(R.string.contact_creation_last_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.NAME_LAST),
                singleLine = true,
            )
        }
    }
}
