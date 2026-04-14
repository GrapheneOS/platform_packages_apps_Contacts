package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
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
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.emailSection(
    emails: List<EmailFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = emails,
        key = { _, item -> item.id },
        contentType = { _, _ -> "email_field" },
    ) { index, email ->
        EmailFieldRow(
            email = email,
            index = index,
            showDelete = emails.size > 1,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "email_add", contentType = "email_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddEmail) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.EMAIL_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_email),
            )
            Text(stringResource(R.string.contact_creation_add_email))
        }
    }
}

@Composable
internal fun EmailFieldRow(
    email: EmailFieldState,
    index: Int,
    showDelete: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        OutlinedTextField(
            value = email.address,
            onValueChange = { onAction(ContactCreationAction.UpdateEmail(email.id, it)) },
            label = { Text(stringResource(R.string.emailLabelsGroup)) },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.emailField(index)),
            singleLine = true,
        )
        if (showDelete) {
            IconButton(
                onClick = { onAction(ContactCreationAction.RemoveEmail(email.id)) },
                modifier = Modifier.testTag(TestTags.emailDelete(index)),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.contact_creation_remove_email)
                )
            }
        }
    }
}
