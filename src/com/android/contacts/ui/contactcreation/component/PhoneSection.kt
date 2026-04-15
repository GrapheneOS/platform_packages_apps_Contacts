package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.PhoneFieldState

/**
 * Phone section as a @Composable for Column-based layout.
 */
@Composable
internal fun PhoneSectionContent(
    phones: List<PhoneFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        phones.forEachIndexed { index, phone ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            PhoneFieldRow(
                phone = phone,
                index = index,
                isFirst = index == 0,
                showDelete = phones.size > 1,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_phone),
            onClick = { onAction(ContactCreationAction.AddPhone) },
            modifier = Modifier.testTag(TestTags.PHONE_ADD),
        )
    }
}

@Composable
internal fun PhoneFieldRow(
    phone: PhoneFieldState,
    index: Int,
    isFirst: Boolean,
    showDelete: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    FieldRow(
        icon = if (isFirst) Icons.Filled.Phone else null,
        modifier = modifier,
        trailing = if (showDelete) {
            {
                IconButton(
                    onClick = { onAction(ContactCreationAction.RemovePhone(phone.id)) },
                    modifier = Modifier.testTag(TestTags.phoneDelete(index)),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.contact_creation_remove_phone),
                    )
                }
            }
        } else {
            null
        },
    ) {
        Column {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.phoneField(index)),
                singleLine = true,
            )
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
