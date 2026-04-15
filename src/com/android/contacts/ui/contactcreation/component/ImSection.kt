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
import com.android.contacts.ui.contactcreation.model.ImFieldState

/**
 * IM section as a @Composable for Column-based layout.
 */
@Composable
internal fun ImSectionContent(
    imAccounts: List<ImFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        imAccounts.forEachIndexed { index, im ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            ImFieldRow(
                im = im,
                index = index,
                onAction = onAction,
            )
        }
        AddRemoveFieldRow(
            addLabel = stringResource(R.string.contact_creation_add_im),
            onAdd = { onAction(ContactCreationAction.AddIm) },
            addTestTag = TestTags.IM_ADD,
            removeLabel = if (imAccounts.size > 1) {
                stringResource(R.string.contact_creation_remove_im)
            } else {
                null
            },
            onRemove = if (imAccounts.size > 1) {
                { onAction(ContactCreationAction.RemoveIm(imAccounts.last().id)) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun ImFieldRow(
    im: ImFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = im.data,
            onValueChange = { onAction(ContactCreationAction.UpdateIm(im.id, it)) },
            label = { Text(stringResource(R.string.imLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.imField(index)),
            singleLine = true,
        )
    }
}
