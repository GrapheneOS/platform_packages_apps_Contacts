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
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState

/**
 * Organization section as a @Composable for Column-based layout.
 * Uses FieldRow for each organization field.
 */
@Composable
internal fun OrganizationSectionContent(
    organization: OrganizationFieldState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FieldRow {
            OutlinedTextField(
                value = organization.company,
                onValueChange = { onAction(ContactCreationAction.UpdateCompany(it)) },
                label = { Text(stringResource(R.string.contact_creation_company)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ORG_COMPANY),
                singleLine = true,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FieldRow {
            OutlinedTextField(
                value = organization.title,
                onValueChange = { onAction(ContactCreationAction.UpdateJobTitle(it)) },
                label = { Text(stringResource(R.string.contact_creation_job_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.ORG_TITLE),
                singleLine = true,
            )
        }
    }
}
