package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.PhoneFieldState

internal fun LazyListScope.phoneSection(
    phones: List<PhoneFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = phones,
        key = { _, item -> item.id },
        contentType = { _, _ -> "phone_field" },
    ) { index, phone ->
        PhoneFieldRow(
            phone = phone,
            index = index,
            showDelete = phones.size > 1,
            onAction = onAction,
            modifier = Modifier.animateItem(),
        )
    }
    item(key = "phone_add", contentType = "phone_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddPhone) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.PHONE_ADD),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add phone")
        }
    }
}

@Composable
internal fun PhoneFieldRow(
    phone: PhoneFieldState,
    index: Int,
    showDelete: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = phone.number,
            onValueChange = { onAction(ContactCreationAction.UpdatePhone(phone.id, it)) },
            label = { Text("Phone") },
            modifier = Modifier
                .weight(1f)
                .testTag(TestTags.phoneField(index)),
            singleLine = true,
        )
        if (showDelete) {
            IconButton(
                onClick = { onAction(ContactCreationAction.RemovePhone(phone.id)) },
                modifier = Modifier.testTag(TestTags.phoneDelete(index)),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove phone")
            }
        }
    }
}
