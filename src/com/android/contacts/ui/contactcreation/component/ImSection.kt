package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
                isFirst = index == 0,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_im),
            onClick = { onAction(ContactCreationAction.AddIm) },
            modifier = Modifier.testTag(TestTags.IM_ADD),
        )
    }
}

@Composable
private fun ImFieldRow(
    im: ImFieldState,
    index: Int,
    isFirst: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        icon = if (isFirst) Icons.AutoMirrored.Filled.Message else null,
        modifier = modifier,
        trailing = {
            IconButton(
                onClick = { onAction(ContactCreationAction.RemoveIm(im.id)) },
                modifier = Modifier.testTag(TestTags.imDelete(index)),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.contact_creation_remove_im),
                )
            }
        },
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
