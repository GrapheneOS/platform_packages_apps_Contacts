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
                onAction = onAction,
            )
        }
        AddRemoveFieldRow(
            addLabel = stringResource(R.string.contact_creation_add_website),
            onAdd = { onAction(ContactCreationAction.AddWebsite) },
            addTestTag = TestTags.WEBSITE_ADD,
            removeLabel = if (websites.size > 1) {
                stringResource(R.string.contact_creation_remove_website)
            } else {
                null
            },
            onRemove = if (websites.size > 1) {
                { onAction(ContactCreationAction.RemoveWebsite(websites.last().id)) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun WebsiteFieldRow(
    website: WebsiteFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FieldRow(
        modifier = modifier,
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
