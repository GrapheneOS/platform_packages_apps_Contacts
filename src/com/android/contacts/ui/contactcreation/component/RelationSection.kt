package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
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
import com.android.contacts.ui.contactcreation.model.RelationFieldState

/**
 * Relation section as a @Composable for Column-based layout.
 */
@Composable
internal fun RelationSectionContent(
    relations: List<RelationFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        relations.forEachIndexed { index, relation ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            RelationFieldRow(
                relation = relation,
                index = index,
                isFirst = index == 0,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_relation),
            onClick = { onAction(ContactCreationAction.AddRelation) },
            modifier = Modifier.testTag(TestTags.RELATION_ADD),
        )
    }
}

@Composable
private fun RelationFieldRow(
    relation: RelationFieldState,
    index: Int,
    isFirst: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        icon = if (isFirst) Icons.Filled.People else null,
        modifier = modifier,
        trailing = {
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.RemoveRelation(relation.id)) },
                contentDescription = stringResource(R.string.contact_creation_remove_relation),
                modifier = Modifier.testTag(TestTags.relationDelete(index)),
            )
        },
    ) {
        OutlinedTextField(
            value = relation.name,
            onValueChange = { onAction(ContactCreationAction.UpdateRelation(relation.id, it)) },
            label = { Text(stringResource(R.string.relationLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.relationField(index)),
            singleLine = true,
        )
    }
}
