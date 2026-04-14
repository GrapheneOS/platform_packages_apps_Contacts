package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.eventItems(
    events: List<EventFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = events,
        key = { _, item -> "event_${item.id}" },
        contentType = { _, _ -> "event_field" },
    ) { index, event ->
        EventFieldRow(
            event = event,
            index = index,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "event_add", contentType = "event_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddEvent) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.EVENT_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_event),
            )
            Text(stringResource(R.string.contact_creation_add_event))
        }
    }
}

@Composable
private fun EventFieldRow(
    event: EventFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        OutlinedTextField(
            value = event.startDate,
            onValueChange = { onAction(ContactCreationAction.UpdateEvent(event.id, it)) },
            label = { Text(stringResource(R.string.contact_creation_date)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.eventField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveEvent(event.id)) },
            modifier = Modifier.testTag(TestTags.eventDelete(index)),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.contact_creation_remove_event),
            )
        }
    }
}
