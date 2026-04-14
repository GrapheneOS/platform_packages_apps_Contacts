package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
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
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.websiteItems(
    websites: List<WebsiteFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = websites,
        key = { _, item -> "website_${item.id}" },
        contentType = { _, _ -> "website_field" },
    ) { index, website ->
        WebsiteFieldRow(
            website = website,
            index = index,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "website_add", contentType = "website_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddWebsite) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.WEBSITE_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_website),
            )
            Text(stringResource(R.string.contact_creation_add_website))
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
        Icon(
            imageVector = Icons.Filled.Public,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        OutlinedTextField(
            value = website.url,
            onValueChange = { onAction(ContactCreationAction.UpdateWebsite(website.id, it)) },
            label = { Text(stringResource(R.string.websiteLabelsGroup)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.websiteField(index)),
            singleLine = true,
        )
        IconButton(
            onClick = { onAction(ContactCreationAction.RemoveWebsite(website.id)) },
            modifier = Modifier.testTag(TestTags.websiteDelete(index)),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.contact_creation_remove_website),
            )
        }
    }
}
