package com.android.contacts.ui.interactions.account.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.core.itemClipShape
import com.android.contacts.ui.interactions.account.common.AccountIcon
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountUiState as State
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_PROGRESS_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SelectAccountDialog(
    effectHandler: SelectAccountEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: SelectAccountScreenModel = viewModel<SelectAccountViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()
    val effectHandler by rememberUpdatedState(effectHandler)

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    SelectAccountDialogContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun SelectAccountDialogContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(
        onDismissRequest = { onAction(Action.Dismiss) },
        sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false,
        ),
        sheetGesturesEnabled = false,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
        content = {
            Column {
                Text(
                    text = stringResource(uiState.titleId ?: R.string.select_account_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 32.dp, bottom = 8.dp),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                                    .size(32.dp)
                                    .testTag(IMPORT_PROGRESS_TEST_TAG),
                            )
                        }

                        else -> {
                            ImportOptionsList(uiState, onAction)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ImportOptionsList(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            items = uiState.accounts.orEmpty(),
            key = { _, account -> "account_${account.hashCode()}" },
        ) { index, account ->
            AccountCell(
                account = account,
                isFirst = index == 0,
                isLast = index == uiState.accounts?.lastIndex,
                onClick = { onAction(Action.AccountSelected(account)) },
            )
        }
    }
}

@Composable
private fun AccountCell(
    account: AccountUiModel,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = itemClipShape(
            isFirst = isFirst,
            isLast = isLast,
            shape = MaterialTheme.shapes.medium,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
        ) {
            AccountIcon(
                account = account,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp),
            )
            Column {
                if (account.type?.isNotBlank() == true) {
                    Text(
                        text = account.type,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (account.name?.isNotBlank() == true && account.name != account.type) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SelectAccountDialogProgressPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            SelectAccountDialogContent(
                uiState = State(),
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SelectAccountDialogListPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            SelectAccountDialogContent(
                uiState = State(
                    accounts = persistentListOf(
                        AccountUiModel(name = "user1@example.org", type = "Google"),
                        AccountUiModel(name = null, type = "Device"),
                    ),
                ),
                onAction = {},
            )
        }
    }
}
