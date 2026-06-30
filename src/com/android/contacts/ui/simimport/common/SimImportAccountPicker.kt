package com.android.contacts.ui.simimport.common

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.android.contacts.R
import com.android.contacts.model.account.AccountDisplayInfo
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
internal fun SimImportAccountPicker(
    list: List<AccountInfo>,
    current: AccountInfo?,
    onChange: (AccountInfo) -> Unit,
) {
    val current = current ?: return
    val canChangeAccounts = list.size > 1
    var isExpanded by remember { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = Modifier
            .testTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        AccountTextField(current, canChangeAccounts, isExpanded)

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            list.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.nameLabel?.toString().orEmpty()) },
                    onClick = {
                        onChange(account)
                        isExpanded = false
                    },
                    leadingIcon = { AccountIcon(account) },
                    trailingIcon = {
                        if (current == account) {
                            Icon(
                                Icons.Filled.Check,
                                modifier = Modifier.size(16.dp),
                                contentDescription = null,
                            )
                        }
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.testTag(TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.AccountTextField(
    current: AccountInfo,
    canChangeAccounts: Boolean,
    isExpanded: Boolean,
) {
    val currentAccountLabel = current.nameLabel?.toString().orEmpty()
    OutlinedTextField(
        value = currentAccountLabel,
        onValueChange = {},
        label = { Text(stringResource(R.string.editor_account_selector_title)) },
        leadingIcon = { AccountIcon(current) },
        trailingIcon = {
            if (canChangeAccounts) {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        },
        singleLine = true,
        enabled = canChangeAccounts,
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
internal fun AccountIcon(account: AccountInfo) {
    Image(
        painter = account.icon?.let { rememberDrawablePainter(it) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .size(24.dp),
    )
}

@PreviewLightDark
@Composable
private fun SimImportAccountPickerSinglePreview() {
    val resources = LocalResources.current
    val context = LocalContext.current
    val icon = ResourcesCompat.getDrawable(
        resources,
        R.drawable.logo_quick_contacts_color_44in48dp,
        context.theme,
    )
    val account = AccountInfo(
        AccountDisplayInfo(
            AccountWithDataSet("user@example.org", null, null),
            "user@example.org",
            null,
            icon,
            false,
        ),
        null,
    )
    ContactsPreviewColumn {
        SimImportAccountPicker(
            list = listOf(account),
            current = account,
            onChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimImportAccountPickerMultiplePreview() {
    val account1 = AccountInfo(
        AccountDisplayInfo(
            AccountWithDataSet("user@example.org", null, null),
            "user@example.org",
            null,
            null,
            false,
        ),
        null,
    )
    val account2 = AccountInfo(
        AccountDisplayInfo(
            AccountWithDataSet("another@example.org", null, null),
            "another@example.org",
            null,
            null,
            false,
        ),
        null,
    )
    ContactsPreviewColumn {
        SimImportAccountPicker(
            list = listOf(account1, account2),
            current = account1,
            onChange = {},
        )
    }
}

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER = "sim_import_account_picker"

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM = "sim_import_account_picker_menu_item"
