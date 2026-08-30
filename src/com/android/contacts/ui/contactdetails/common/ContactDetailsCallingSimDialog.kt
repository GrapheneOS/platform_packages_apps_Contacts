package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CALLING_SIM_DIALOG_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CallingSimAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.CallingSimNumberUiModel
import com.android.contacts.ui.contactdetails.screen.model.CallingSimSelection
import com.android.contacts.ui.contactdetails.screen.model.CallingSimUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactDetailsCallingSimDialog(
    callingSim: CallingSimUiModel,
    onConfirm: (List<CallingSimSelection>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(CALLING_SIM_DIALOG_TEST_TAG),
    ) {
        CallingSimContent(
            callingSim = callingSim,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CallingSimContent(
    callingSim: CallingSimUiModel,
    onConfirm: (List<CallingSimSelection>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selections = remember(callingSim) {
        mutableStateMapOf<Long, PhoneAccountId>().apply {
            callingSim.numbers.forEach { number ->
                number.selectedAccountId?.let { accountId ->
                    put(number.dataId, accountId)
                }
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column {
            Text(
                text = stringResource(R.string.contact_details_set_calling_sim),
                style = MaterialTheme.typography.headlineSmall,
                color = AlertDialogDefaults.titleContentColor,
                modifier = Modifier.padding(
                    start = Tokens.callingSimContentPadding,
                    end = Tokens.callingSimContentPadding,
                    top = Tokens.callingSimContentPadding,
                    bottom = Tokens.callingSimTitleSpacing,
                ),
            )

            Column(
                modifier = Modifier
                    .weight(
                        weight = 1f,
                        fill = false,
                    )
                    .verticalScroll(rememberScrollState()),
            ) {
                callingSim.numbers.forEach { number ->
                    CallingSimNumber(
                        number = number,
                        accounts = callingSim.accounts,
                        selectedAccountId = selections[number.dataId],
                        onAccountSelected = { accountId ->
                            selections[number.dataId] = accountId
                        },
                    )
                }
            }

            CallingSimButtons(
                isResetVisible = callingSim.numbers.any { number ->
                    number.selectedAccountId != null
                },
                isConfirmEnabled = callingSim.numbers.any { number ->
                    selections[number.dataId] != number.selectedAccountId
                },
                onReset = { selections.clear() },
                onConfirm = { onConfirm(pickedSelections(callingSim, selections)) },
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun CallingSimNumber(
    number: CallingSimNumberUiModel,
    accounts: List<CallingSimAccountUiModel>,
    selectedAccountId: PhoneAccountId?,
    onAccountSelected: (PhoneAccountId) -> Unit,
) {
    Column {
        Text(
            text = number.number,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = Tokens.callingSimContentPadding,
                end = Tokens.callingSimContentPadding,
                top = Tokens.callingSimNumberSpacing,
            ),
        )

        val numberLabel = number.numberLabel
        if (numberLabel != null) {
            Text(
                text = numberLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = Tokens.callingSimContentPadding),
            )
        }

        accounts.forEach { account ->
            CallingSimAccount(
                account = account,
                isSelected = account.accountId == selectedAccountId,
                onSelected = { onAccountSelected(account.accountId) },
            )
        }
    }
}

@Composable
private fun CallingSimAccount(
    account: CallingSimAccountUiModel,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onSelected,
            )
            .padding(horizontal = Tokens.callingSimContentPadding),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            modifier = Modifier.minimumInteractiveComponentSize(),
        )

        Text(
            text = account.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Tokens.callingSimAccountSpacing),
        )
    }
}

@Composable
private fun CallingSimButtons(
    isResetVisible: Boolean,
    isConfirmEnabled: Boolean,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            space = Tokens.callingSimAccountSpacing,
            alignment = Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Tokens.callingSimContentPadding),
    ) {
        if (isResetVisible) {
            TextButton(onClick = onReset) {
                Text(text = stringResource(R.string.contact_details_calling_sim_reset))
            }
        }

        TextButton(onClick = onDismiss) {
            Text(text = stringResource(android.R.string.cancel))
        }

        TextButton(onClick = onConfirm, enabled = isConfirmEnabled) {
            Text(text = stringResource(android.R.string.ok))
        }
    }
}

private fun pickedSelections(
    callingSim: CallingSimUiModel,
    selections: Map<Long, PhoneAccountId>,
): List<CallingSimSelection> {
    return callingSim.numbers.map { number ->
        CallingSimSelection(
            dataId = number.dataId,
            accountId = selections[number.dataId],
        )
    }
}

private fun previewAccountId(id: String): PhoneAccountId {
    return PhoneAccountId(componentName = "com.android.phone/Sim", id = id)
}

@PreviewLightDark
@Composable
private fun ContactDetailsCallingSimDialogPreview() {
    ContactsPreviewColumn {
        CallingSimContent(
            callingSim = CallingSimUiModel(
                accounts = persistentListOf(
                    CallingSimAccountUiModel(previewAccountId("1"), "+31612345678"),
                    CallingSimAccountUiModel(previewAccountId("2"), "+15550001"),
                ),
                numbers = persistentListOf(
                    CallingSimNumberUiModel(
                        dataId = 1L,
                        number = "+31687654321",
                        numberLabel = "Mobile",
                        selectedAccountId = previewAccountId("1"),
                    ),
                    CallingSimNumberUiModel(
                        dataId = 2L,
                        number = "+15550000",
                        numberLabel = "Work",
                        selectedAccountId = previewAccountId("1"),
                    ),
                ),
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
