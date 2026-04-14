package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.core.animateItemIfMotionAllowed

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
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "phone_add", contentType = "phone_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddPhone) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.PHONE_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_phone),
            )
            Text(stringResource(R.string.contact_creation_add_phone))
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
    var showCustomDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            FieldTypeSelector(
                currentType = phone.type,
                types = PhoneType.selectorTypes,
                typeLabel = { it.label() },
                onTypeSelected = { selected ->
                    if (selected is PhoneType.Custom && selected.label.isEmpty()) {
                        showCustomDialog = true
                    } else {
                        onAction(ContactCreationAction.UpdatePhoneType(phone.id, selected))
                    }
                },
                modifier = Modifier.testTag(TestTags.phoneType(index)),
            )
            OutlinedTextField(
                value = phone.number,
                onValueChange = { onAction(ContactCreationAction.UpdatePhone(phone.id, it)) },
                label = { Text(stringResource(R.string.phoneLabelsGroup)) },
                modifier = Modifier.testTag(TestTags.phoneField(index)),
                singleLine = true,
            )
        }
        if (showDelete) {
            IconButton(
                onClick = { onAction(ContactCreationAction.RemovePhone(phone.id)) },
                modifier = Modifier.testTag(TestTags.phoneDelete(index)),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.contact_creation_remove_phone)
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomLabelDialog(
            onConfirm = { label ->
                showCustomDialog = false
                onAction(ContactCreationAction.UpdatePhoneType(phone.id, PhoneType.Custom(label)))
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}
