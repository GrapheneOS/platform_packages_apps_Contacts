package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState

internal fun LazyListScope.organizationSection(
    organization: OrganizationFieldState,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "organization_section", contentType = "organization_section") {
        OrganizationFields(organization = organization, onAction = onAction)
    }
}

@Composable
internal fun OrganizationFields(
    organization: OrganizationFieldState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Business,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp, top = 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = organization.company,
                onValueChange = { onAction(ContactCreationAction.UpdateCompany(it)) },
                label = { Text("Company") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ORG_COMPANY),
                singleLine = true,
            )
            OutlinedTextField(
                value = organization.title,
                onValueChange = { onAction(ContactCreationAction.UpdateJobTitle(it)) },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ORG_TITLE),
                singleLine = true,
            )
        }
    }
}
