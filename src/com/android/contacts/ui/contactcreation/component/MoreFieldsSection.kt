@file:Suppress("TooManyFunctions")

package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

@Suppress("LongParameterList")
internal fun LazyListScope.moreFieldsSection(
    isExpanded: Boolean,
    events: List<EventFieldState>,
    relations: List<RelationFieldState>,
    imAccounts: List<ImFieldState>,
    websites: List<WebsiteFieldState>,
    note: String,
    nickname: String,
    sipAddress: String,
    showSipField: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    moreFieldsToggle(isExpanded, onAction)
    moreFieldsContent(isExpanded, nickname, note, sipAddress, showSipField, onAction)
    if (isExpanded) {
        eventItems(events, onAction)
        relationItems(relations, onAction)
        imItems(imAccounts, onAction)
        websiteItems(websites, onAction)
    }
}

private fun LazyListScope.moreFieldsToggle(
    isExpanded: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "more_fields_toggle", contentType = "more_fields_toggle") {
        TextButton(
            onClick = { onAction(ContactCreationAction.ToggleMoreFields) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.MORE_FIELDS_TOGGLE),
        ) {
            Icon(
                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
            )
            Text(if (isExpanded) "Less fields" else "More fields")
        }
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.moreFieldsContent(
    isExpanded: Boolean,
    nickname: String,
    note: String,
    sipAddress: String,
    showSipField: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "more_fields_content", contentType = "more_fields_content") {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier.testTag(TestTags.MORE_FIELDS_CONTENT),
        ) {
            MoreFieldsSingleFields(nickname, note, sipAddress, showSipField, onAction)
        }
    }
}

@Composable
private fun MoreFieldsSingleFields(
    nickname: String,
    note: String,
    sipAddress: String,
    showSipField: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    Column {
        OutlinedTextField(
            value = nickname,
            onValueChange = { onAction(ContactCreationAction.UpdateNickname(it)) },
            label = { Text("Nickname") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag(TestTags.NICKNAME_FIELD),
            singleLine = true,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { onAction(ContactCreationAction.UpdateNote(it)) },
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag(TestTags.NOTE_FIELD),
        )
        if (showSipField) {
            OutlinedTextField(
                value = sipAddress,
                onValueChange = { onAction(ContactCreationAction.UpdateSipAddress(it)) },
                label = { Text("SIP") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(TestTags.SIP_FIELD),
                singleLine = true,
            )
        }
    }
}

// --- Events ---

private fun LazyListScope.eventItems(
    events: List<EventFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = events,
        key = { _, item -> "event_${item.id}" },
        contentType = { _, _ -> "event_field" },
    ) { index, event ->
        EventFieldRow(event = event, index = index, onAction = onAction)
    }
    item(key = "event_add", contentType = "event_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddEvent) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.EVENT_ADD),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add event")
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
        OutlinedTextField(
            value = event.startDate,
            onValueChange = { onAction(ContactCreationAction.UpdateEvent(event.id, it)) },
            label = { Text("Date") },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.eventField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveEvent(event.id)) },
            modifier = Modifier.testTag(TestTags.eventDelete(index)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove event")
        }
    }
}

// --- Relations ---

private fun LazyListScope.relationItems(
    relations: List<RelationFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = relations,
        key = { _, item -> "relation_${item.id}" },
        contentType = { _, _ -> "relation_field" },
    ) { index, relation ->
        RelationFieldRow(relation = relation, index = index, onAction = onAction)
    }
    item(key = "relation_add", contentType = "relation_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddRelation) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.RELATION_ADD),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add relation")
        }
    }
}

@Composable
private fun RelationFieldRow(
    relation: RelationFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = relation.name,
            onValueChange = { onAction(ContactCreationAction.UpdateRelation(relation.id, it)) },
            label = { Text("Relation") },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.relationField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveRelation(relation.id)) },
            modifier = Modifier.testTag(TestTags.relationDelete(index)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove relation")
        }
    }
}

// --- IM ---

private fun LazyListScope.imItems(
    imAccounts: List<ImFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = imAccounts,
        key = { _, item -> "im_${item.id}" },
        contentType = { _, _ -> "im_field" },
    ) { index, im ->
        ImFieldRow(im = im, index = index, onAction = onAction)
    }
    item(key = "im_add", contentType = "im_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddIm) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.IM_ADD),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add IM")
        }
    }
}

@Composable
private fun ImFieldRow(
    im: ImFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = im.data,
            onValueChange = { onAction(ContactCreationAction.UpdateIm(im.id, it)) },
            label = { Text("IM") },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.imField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveIm(im.id)) },
            modifier = Modifier.testTag(TestTags.imDelete(index)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove IM")
        }
    }
}

// --- Website ---

private fun LazyListScope.websiteItems(
    websites: List<WebsiteFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = websites,
        key = { _, item -> "website_${item.id}" },
        contentType = { _, _ -> "website_field" },
    ) { index, website ->
        WebsiteFieldRow(website = website, index = index, onAction = onAction)
    }
    item(key = "website_add", contentType = "website_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddWebsite) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.WEBSITE_ADD),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add website")
        }
    }
}

@Composable
private fun WebsiteFieldRow(
    website: WebsiteFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = website.url,
            onValueChange = { onAction(ContactCreationAction.UpdateWebsite(website.id, it)) },
            label = { Text("Website") },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.websiteField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveWebsite(website.id)) },
            modifier = Modifier.testTag(TestTags.websiteDelete(index)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove website")
        }
    }
}
