package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
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
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

/**
 * Website section as a @Composable for Column-based layout.
 */
@Composable
internal fun WebsiteSectionContent(
    websites: List<WebsiteFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        websites.forEachIndexed { index, website ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            WebsiteFieldRow(
                website = website,
                index = index,
                isFirst = index == 0,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_website),
            onClick = { onAction(ContactCreationAction.AddWebsite) },
            modifier = Modifier.testTag(TestTags.WEBSITE_ADD),
        )
    }
}

@Composable
private fun WebsiteFieldRow(
    website: WebsiteFieldState,
    index: Int,
    isFirst: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        icon = if (isFirst) Icons.Filled.Public else null,
        modifier = modifier,
        trailing = {
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.RemoveWebsite(website.id)) },
                contentDescription = stringResource(R.string.contact_creation_remove_website),
                modifier = Modifier.testTag(TestTags.websiteDelete(index)),
            )
        },
    ) {
        OutlinedTextField(
            value = website.url,
            onValueChange = { onAction(ContactCreationAction.UpdateWebsite(website.id, it)) },
            label = { Text(stringResource(R.string.websiteLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.websiteField(index)),
            singleLine = true,
        )
    }
}
