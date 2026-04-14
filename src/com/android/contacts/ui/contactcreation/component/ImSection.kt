package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
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
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.imItems(
    imAccounts: List<ImFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = imAccounts,
        key = { _, item -> "im_${item.id}" },
        contentType = { _, _ -> "im_field" },
    ) { index, im ->
        ImFieldRow(
            im = im,
            index = index,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "im_add", contentType = "im_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddIm) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.IM_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_im),
            )
            Text(stringResource(R.string.contact_creation_add_im))
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
        Icon(
            imageVector = Icons.Filled.Message,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        OutlinedTextField(
            value = im.data,
            onValueChange = { onAction(ContactCreationAction.UpdateIm(im.id, it)) },
            label = { Text(stringResource(R.string.imLabelsGroup)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.imField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveIm(im.id)) },
            modifier = Modifier.testTag(TestTags.imDelete(index)),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.contact_creation_remove_im),
            )
        }
    }
}
