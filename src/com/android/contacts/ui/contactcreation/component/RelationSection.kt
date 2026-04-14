package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
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
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.relationItems(
    relations: List<RelationFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = relations,
        key = { _, item -> "relation_${item.id}" },
        contentType = { _, _ -> "relation_field" },
    ) { index, relation ->
        RelationFieldRow(
            relation = relation,
            index = index,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "relation_add", contentType = "relation_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddRelation) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.RELATION_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_relation),
            )
            Text(stringResource(R.string.contact_creation_add_relation))
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
        Icon(
            imageVector = Icons.Filled.People,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        OutlinedTextField(
            value = relation.name,
            onValueChange = { onAction(ContactCreationAction.UpdateRelation(relation.id, it)) },
            label = { Text(stringResource(R.string.relationLabelsGroup)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.relationField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveRelation(relation.id)) },
            modifier = Modifier.testTag(TestTags.relationDelete(index)),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.contact_creation_remove_relation),
            )
        }
    }
}
