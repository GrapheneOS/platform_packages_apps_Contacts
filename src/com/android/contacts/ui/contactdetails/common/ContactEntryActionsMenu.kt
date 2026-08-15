package com.android.contacts.ui.contactdetails.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel

private val ActionsMenuWidth = 220.dp

@Composable
internal fun ContactEntryActionsMenu(
    entry: ContactEntryUiModel,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    onCopyClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    onClearDefaultClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        modifier = modifier.width(ActionsMenuWidth),
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
    ) {
        if (entry.copyText != null) {
            ContactEntryActionsMenuItem(
                labelResource = R.string.copy_text,
                onClick = {
                    onCopyClick()
                    onDismissRequest()
                },
            )
        }

        when {
            !entry.isDefaultChangeable -> Unit

            entry.isSuperPrimary -> ContactEntryActionsMenuItem(
                labelResource = R.string.clear_default,
                onClick = {
                    onClearDefaultClick()
                    onDismissRequest()
                },
            )

            else -> ContactEntryActionsMenuItem(
                labelResource = R.string.set_default,
                onClick = {
                    onSetDefaultClick()
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
private fun ContactEntryActionsMenuItem(
    @StringRes labelResource: Int,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(text = stringResource(labelResource))
        },
        onClick = onClick,
    )
}
