package com.android.contacts.ui.simimport.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_ACCOUNT_PICKER_TEST_TAG
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SimImportAccountPicker(
    list: ImmutableList<AccountUiModel>,
    current: AccountUiModel?,
    onChange: (AccountUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = current ?: return
    val canChangeAccounts = list.size > 1
    var isExpanded by remember { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            if (canChangeAccounts) {
                isExpanded = it
            }
        },
        modifier = modifier
            .testTag(SIM_IMPORT_ACCOUNT_PICKER_TEST_TAG)
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
                    text = { Text(account.name.orEmpty()) },
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
                    modifier = Modifier.testTag(SIM_IMPORT_ACCOUNT_PICKER_MENU_ITEM_TEST_TAG),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.AccountTextField(
    current: AccountUiModel,
    canChangeAccounts: Boolean,
    isExpanded: Boolean,
) {
    val currentAccountLabel = current.name.orEmpty()
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
internal fun AccountIcon(account: AccountUiModel) {
    Image(
        painter = account.icon?.let { rememberDrawablePainter(it) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
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
    val account = AccountUiModel("user@example.org")
    ContactsPreviewColumn {
        SimImportAccountPicker(
            list = persistentListOf(account),
            current = account,
            onChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimImportAccountPickerMultiplePreview() {
    val account1 = AccountUiModel("user@example.org")
    val account2 = AccountUiModel("another@example.org")
    ContactsPreviewColumn {
        SimImportAccountPicker(
            list = persistentListOf(account1, account2),
            current = account1,
            onChange = {},
        )
    }
}
