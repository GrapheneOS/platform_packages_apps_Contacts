package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.NameState

internal fun LazyListScope.nameSection(
    nameState: NameState,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "name_section", contentType = "name_section") {
        NameFields(nameState = nameState, onAction = onAction)
    }
}

@Composable
internal fun NameFields(
    nameState: NameState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp, top = 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = nameState.first,
                onValueChange = { onAction(ContactCreationAction.UpdateFirstName(it)) },
                label = { Text(stringResource(R.string.contact_creation_first_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.NAME_FIRST),
                singleLine = true,
            )
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
