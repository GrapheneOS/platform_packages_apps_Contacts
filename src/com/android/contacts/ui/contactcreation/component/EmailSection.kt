package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.android.contacts.ui.core.isReduceMotionEnabled

/**
 * Email section as a @Composable for Column-based layout.
 */
@Composable
internal fun EmailSectionContent(
    emails: List<EmailFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = isReduceMotionEnabled()
    Column(
        modifier = modifier.animateContentSize(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ),
    ) {
        emails.forEachIndexed { index, email ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            val visibleState = remember {
                MutableTransitionState(false).apply { targetState = true }
            }
            AnimatedVisibility(
                visibleState = visibleState,
                enter = if (reduceMotion) EnterTransition.None else expandVertically() + fadeIn(),
            ) {
                EmailFieldRow(
                    email = email,
                    index = index,
                    onAction = onAction,
                )
            }
        }
        AddRemoveFieldRow(
            addLabel = stringResource(R.string.contact_creation_add_email),
            onAdd = { onAction(ContactCreationAction.AddEmail) },
            addTestTag = TestTags.EMAIL_ADD,
            removeLabel = if (emails.size > 1) {
                stringResource(R.string.contact_creation_remove_email)
            } else {
                null
            },
            onRemove = if (emails.size > 1) {
                { onAction(ContactCreationAction.RemoveEmail(emails.last().id)) }
            } else {
                null
            },
        )
    }
}

@Composable
internal fun EmailFieldRow(
    email: EmailFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentTypeLabel = email.type.label(context)

    FieldRow(modifier = modifier) {
        OutlinedTextField(
            value = email.address,
            onValueChange = { onAction(ContactCreationAction.UpdateEmail(email.id, it)) },
            label = {
                Text("${stringResource(R.string.emailLabelsGroup)} ($currentTypeLabel)")
            },
            trailingIcon = {
                EmailTypeDropdown(
                    index = index,
                    onTypeSelected = { type ->
                        if (type is EmailType.Custom && type.label.isEmpty()) {
                            showCustomDialog = true
                        } else {
                            onAction(ContactCreationAction.UpdateEmailType(email.id, type))
                        }
                    },
                )
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

@Composable
private fun EmailTypeDropdown(
    index: Int,
    onTypeSelected: (EmailType) -> Unit,
) {
    var typeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectorLabels = remember { EmailType.selectorTypes.map { it.label(context) } }

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
                    onTypeSelected(type)
                },
                modifier = Modifier.testTag(
                    TestTags.fieldTypeOption(selectorLabels[i])
                ),
            )
        }
    }
}
