package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
                onAction = onAction,
            )
        }
        AddRemoveFieldRow(
            addLabel = stringResource(R.string.contact_creation_add_event),
            onAdd = { onAction(ContactCreationAction.AddEvent) },
            addTestTag = TestTags.EVENT_ADD,
            removeLabel = if (events.size > 1) {
                stringResource(R.string.contact_creation_remove_event)
            } else {
                null
            },
            onRemove = if (events.size > 1) {
                { onAction(ContactCreationAction.RemoveEvent(events.last().id)) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun EventFieldRow(
    event: EventFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        modifier = modifier,
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
