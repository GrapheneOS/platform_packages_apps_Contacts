package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.EmailFieldState

/**
 * Email section as a @Composable for Column-based layout.
 */
@Composable
internal fun EmailSectionContent(
    emails: List<EmailFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        emails.forEachIndexed { index, email ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            EmailFieldRow(
                email = email,
                index = index,
                isFirst = index == 0,
                showDelete = emails.size > 1,
                onAction = onAction,
            )
        }
        AddFieldButton(
            label = stringResource(R.string.contact_creation_add_email),
            onClick = { onAction(ContactCreationAction.AddEmail) },
            modifier = Modifier.testTag(TestTags.EMAIL_ADD),
        )
    }
}

@Composable
internal fun EmailFieldRow(
    email: EmailFieldState,
    index: Int,
    isFirst: Boolean,
    showDelete: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val selectorLabels = remember { EmailType.selectorTypes.map { it.label(context) } }

    FieldRow(
        icon = if (isFirst) Icons.Filled.Email else null,
        modifier = modifier,
        trailing = if (showDelete) {
            {
                RemoveFieldButton(
                    onClick = { onAction(ContactCreationAction.RemoveEmail(email.id)) },
                    contentDescription = stringResource(R.string.contact_creation_remove_email),
                    modifier = Modifier.testTag(TestTags.emailDelete(index)),
                )
            }
        } else {
            null
        },
    ) {
        OutlinedTextField(
            value = email.address,
            onValueChange = { onAction(ContactCreationAction.UpdateEmail(email.id, it)) },
            label = {
                Text(
                    "${stringResource(R.string.emailLabelsGroup)} (${email.type.label(context)})",
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.testTag(TestTags.emailType(index)),
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = stringResource(R.string.contact_creation_change_type),
                    )
                }
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                ) {
                    EmailType.selectorTypes.forEachIndexed { i, type ->
                        DropdownMenuItem(
                            text = { Text(selectorLabels[i]) },
                            onClick = {
                                typeExpanded = false
                                if (type is EmailType.Custom && type.label.isEmpty()) {
                                    showCustomDialog = true
                                } else {
                                    onAction(
                                        ContactCreationAction.UpdateEmailType(email.id, type),
                                    )
                                }
                            },
                            modifier = Modifier.testTag(
                                TestTags.fieldTypeOption(selectorLabels[i])
                            ),
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.emailField(index)),
            singleLine = true,
        )
    }

    if (showCustomDialog) {
        CustomLabelDialog(
            onConfirm = { label ->
                showCustomDialog = false
                onAction(ContactCreationAction.UpdateEmailType(email.id, EmailType.Custom(label)))
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}
