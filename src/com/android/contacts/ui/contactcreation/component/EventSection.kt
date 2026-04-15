package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
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
import com.android.contacts.ui.contactcreation.model.EventFieldState

/**
 * Event section as a @Composable for Column-based layout.
 */
@Composable
internal fun EventSectionContent(
    events: List<EventFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        events.forEachIndexed { index, event ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            EventFieldRow(
                event = event,
                index = index,
                isFirst = index == 0,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_event),
            onClick = { onAction(ContactCreationAction.AddEvent) },
            modifier = Modifier.testTag(TestTags.EVENT_ADD),
        )
    }
}

@Composable
private fun EventFieldRow(
    event: EventFieldState,
    index: Int,
    isFirst: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        icon = if (isFirst) Icons.Filled.Event else null,
        modifier = modifier,
        trailing = {
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.RemoveEvent(event.id)) },
                contentDescription = stringResource(R.string.contact_creation_remove_event),
                modifier = Modifier.testTag(TestTags.eventDelete(index)),
            )
        },
    ) {
        OutlinedTextField(
            value = event.startDate,
            onValueChange = { onAction(ContactCreationAction.UpdateEvent(event.id, it)) },
            label = { Text(stringResource(R.string.contact_creation_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.eventField(index)),
            singleLine = true,
        )
    }
}
