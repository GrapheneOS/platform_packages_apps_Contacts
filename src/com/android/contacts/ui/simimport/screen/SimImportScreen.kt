package com.android.contacts.ui.simimport.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.simimport.common.SimContactCell
import com.android.contacts.ui.simimport.common.SimContactSelectableCell
import com.android.contacts.ui.simimport.common.SimImportAccountPicker
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_DESELECT_ALL_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_IMPORT_BUTTON_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SIM_IMPORT_SELECT_ALL_TEST_TAG
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import com.android.contacts.ui.simimport.screen.model.SimImportUiState as State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SimImportScreen(
    effectHandler: SimImportEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: SimImportScreenModel = viewModel<SimImportViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    SimImportContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun SimImportContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            SimImportTopAppBar(
                uiState = uiState,
                onAction = onAction,
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        Column(
            Modifier.padding(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
            ),
        ) {
            if (uiState is State.WithAccounts) {
                SimImportAccountPicker(
                    list = uiState.accounts,
                    current = uiState.currentAccount,
                    onChange = { onAction(Action.AccountChanged(it)) },
                )
            }

            Box(Modifier.fillMaxSize()) {
                SimImportBody(
                    uiState = uiState,
                    onAction = onAction,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun SimImportTopAppBar(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onAction(Action.CloseClicked) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription =
                        stringResource(R.string.sim_import_cancel_content_description),
                )
            }
        },
        title = { SimImportTitle(uiState) },
        actions = {
            if (uiState is State.Ready) {
                IconButton(
                    onClick = { onAction(Action.SelectAllClicked) },
                    enabled = uiState.isSelectAllEnabled,
                    modifier = Modifier.testTag(SIM_IMPORT_SELECT_ALL_TEST_TAG),
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = stringResource(
                            R.string.sim_import_select_all_contacts,
                        ),
                    )
                }
                IconButton(
                    onClick = { onAction(Action.DeselectAllClicked) },
                    enabled = uiState.isDeselectAllEnabled,
                    modifier = Modifier.testTag(SIM_IMPORT_DESELECT_ALL_TEST_TAG),
                ) {
                    Icon(
                        imageVector = Icons.Default.Deselect,
                        contentDescription = stringResource(
                            R.string.sim_import_deselect_all_contacts,
                        ),
                    )
                }
                TextButton(
                    onClick = { onAction(Action.ImportClicked) },
                    enabled = uiState.isImportEnabled,
                    modifier = Modifier.testTag(SIM_IMPORT_IMPORT_BUTTON_TEST_TAG),
                ) {
                    Text(stringResource(R.string.sim_import_button_text))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SimImportTitle(uiState: State) {
    Text(
        text = when {
            uiState is State.Ready && uiState.selectedContactsCount > 0 ->
                pluralStringResource(
                    R.plurals.sim_import_selected_contacts_title,
                    uiState.selectedContactsCount,
                    uiState.selectedContactsCount,
                )
            else -> stringResource(R.string.sim_import_title_none_selected)
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun BoxScope.SimImportBody(
    uiState: State,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
) {
    when (uiState) {
        State.Loading -> {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.Center)
                    .size(64.dp),
            )
        }

        State.NoAccounts -> {
            EmptyState(
                messageRes = R.string.sim_import_empty_accounts_message,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        is State.NoContacts -> {
            EmptyState(
                messageRes = R.string.sim_import_empty_message,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        is State.Ready -> {
            ContactsList(
                contentPadding = contentPadding,
                contactsToImport = uiState.contactsToImport,
                contactsAlreadyImported = uiState.contactsAlreadyImported,
                onContactSelectionChange = { contact, isSelected ->
                    onAction(Action.ContactSelectionChanged(contact, isSelected))
                },
            )
        }
    }
}

@Composable
private fun EmptyState(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(36.dp),
    )
}

@Composable
private fun ContactsList(
    contentPadding: PaddingValues,
    contactsToImport: ImmutableList<SelectableItem<SimContactUiModel>>,
    contactsAlreadyImported: ImmutableList<SimContactUiModel>,
    onContactSelectionChange: (SimContactUiModel, Boolean) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 32.dp + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (contactsToImport.any()) {
            stickyHeader(key = "contacts_to_import") {
                Text(
                    text = stringResource(R.string.sim_import_contacts_list_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .testTag(SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE_TEST_TAG)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(top = 16.dp, bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                )
            }
            itemsIndexed(
                contactsToImport,
                key = { _, item -> item.item.recordNumber },
            ) { index, item ->
                SimContactSelectableCell(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == contactsToImport.lastIndex,
                    onSelectedChange = { onContactSelectionChange(item.item, it) },
                )
            }
        }
        if (contactsAlreadyImported.any()) {
            stickyHeader(key = "contacts_already_imported") {
                Text(
                    text = stringResource(R.string.sim_import_existing_contacts_list_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(top = 16.dp, bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                )
            }
            itemsIndexed(
                contactsAlreadyImported,
                key = { _, item -> item.recordNumber },
            ) { index, contact ->
                SimContactCell(
                    contact = contact,
                    isFirst = index == 0,
                    isLast = index == contactsAlreadyImported.lastIndex,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SimImportScreenPreview() {
    val icon = ResourcesCompat.getDrawable(
        LocalResources.current,
        R.drawable.logo_quick_contacts_color_44in48dp,
        LocalContext.current.theme,
    )
    val account = AccountUiModel(name = "user@example.org", icon = icon)
    ContactsPreviewTheme {
        SimImportContent(
            uiState = State.Ready(
                accounts = persistentListOf(account),
                currentAccount = account,
                contactsToImport = persistentListOf(
                    SelectableItem(
                        item = SimContactUiModel(1, "Anna Smith"),
                        isSelected = false,
                    ),
                    SelectableItem(
                        item = SimContactUiModel(2, "Bob Smith"),
                        isSelected = true,
                    ),
                ),
                contactsAlreadyImported = persistentListOf(
                    SimContactUiModel(3, "Carl Smith"),
                ),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimImportScreenLoadingPreview() {
    ContactsPreviewTheme {
        SimImportContent(
            uiState = State.Loading,
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimImportScreenNoAccountsPreview() {
    ContactsPreviewTheme {
        SimImportContent(
            uiState = State.NoAccounts,
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimImportScreenNoContactsPreview() {
    val icon = ResourcesCompat.getDrawable(
        LocalResources.current,
        R.drawable.logo_quick_contacts_color_44in48dp,
        LocalContext.current.theme,
    )
    val account = AccountUiModel(name = "user@example.org", icon = icon)
    ContactsPreviewTheme {
        SimImportContent(
            uiState = State.NoContacts(
                accounts = persistentListOf(account),
                currentAccount = account,
            ),
            onAction = {},
        )
    }
}
