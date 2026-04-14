package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = nameState.first,
            onValueChange = { onAction(ContactCreationAction.UpdateFirstName(it)) },
            label = { Text("First name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NAME_FIRST),
            singleLine = true,
        )
        OutlinedTextField(
            value = nameState.last,
            onValueChange = { onAction(ContactCreationAction.UpdateLastName(it)) },
            label = { Text("Last name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NAME_LAST),
            singleLine = true,
        )
    }
}
