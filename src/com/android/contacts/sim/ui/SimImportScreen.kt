package com.android.contacts.sim.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.android.contacts.R
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountDisplayInfo
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.sim.ui.SimImportViewModel
import com.android.contacts.ui.core.AppScaffold
import com.android.contacts.ui.core.AppTheme

@Composable
fun SimImportScreen(
    state: SimImportViewModel.State,
    onEvent: (SimImportViewModel.Event) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        SimImportTopAppBar(
            state = state,
            onEvent = onEvent,
            onClose = onClose
        )

        state.currentAccount?.let {
            SimImportAccountPicker(
                list = state.accounts,
                current = state.currentAccount,
                onChange = { onEvent(SimImportViewModel.Event.AccountChanged(it)) },
            )
        }

        Box(Modifier.fillMaxSize()) {
            if (state.contactsToImport.any() || state.contactsAlreadyImported.any()) {
                ContactsList(
                    contactsToImport = state.contactsToImport,
                    contactsAlreadyImported = state.contactsAlreadyImported,
                    onContactClicked = { onEvent(SimImportViewModel.Event.ContactClicked(it)) },
                )
            } else if (!state.isLoading) {
                Text(
                    text = stringResource(R.string.sim_import_empty_message),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(36.dp),
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .size(64.dp),
                )
            }
        }
    }

    if (state.close) {
        LaunchedEffect(Unit) { onClose() }
    }
}

@Composable
private fun SimImportTopAppBar(
    state: SimImportViewModel.State,
    onEvent: (SimImportViewModel.Event) -> Unit,
    onClose: () -> Unit,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(
                        R.string.sim_import_cancel_content_description,
                    ),
                )
            }
        },
        title = {
            Text(
                text = if (state.selectedContactsCount > 0) {
                    state.selectedContactsCount.toString()
                } else {
                    stringResource(R.string.sim_import_title_none_selected)
                },
            )
        },
        actions = {
            IconButton(
                onClick = { onEvent(SimImportViewModel.Event.SelectAllClicked) },
                enabled = state.isSelectAllEnabled,
                modifier = Modifier.testTag(TEST_TAG_SIM_IMPORT_SELECT_ALL),
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = stringResource(R.string.sim_import_select_all_contacts),
                )
            }
            IconButton(
                onClick = { onEvent(SimImportViewModel.Event.DeselectAllClicked) },
                enabled = state.isDeselectAllEnabled,
                modifier = Modifier.testTag(TEST_TAG_SIM_IMPORT_DESELECT_ALL),
            ) {
                Icon(
                    imageVector = Icons.Default.Deselect,
                    contentDescription = stringResource(
                        R.string.sim_import_deselect_all_contacts,
                    ),
                )
            }
            TextButton(
                onClick = { onEvent(SimImportViewModel.Event.ImportClicked) },
                enabled = state.isImportEnabled,
                modifier = Modifier.testTag(TEST_TAG_SIM_IMPORT_IMPORT_BUTTON),
            ) {
                Text(stringResource(R.string.sim_import_button_text))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun ContactsList(
    contactsToImport: List<SelectableItem<SimContact>>,
    contactsAlreadyImported: List<SimContact>,
    onContactClicked: (SimContact) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
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
                        .testTag(TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE)
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
                    onClick = { onContactClicked(item.item) },
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

@Composable
@Preview
fun SimImportScreenPreview() {
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
    AppTheme {
        AppScaffold {
            SimImportScreen(
                state = SimImportViewModel.State(
                    isLoading = true,
                    accounts = listOf(account),
                    currentAccount = account,
                    contactsToImport = listOf(
                        SelectableItem(
                            item = SimContact(1, "Anna Smith", null),
                            isSelected = false,
                        ),
                        SelectableItem(
                            item = SimContact(2, "Bob Smith", null),
                            isSelected = true,
                        ),
                    ),
                    contactsAlreadyImported = listOf(
                        SimContact(3, "Carl Smith", null),
                    ),
                ),
                onEvent = {},
                onClose = {},
            )
        }
    }
}

@Composable
@Preview
fun SimImportScreenWithoutContactsPreview() {
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
    AppTheme {
        AppScaffold {
            SimImportScreen(
                state = SimImportViewModel.State(
                    isLoading = false,
                    accounts = listOf(account),
                    currentAccount = account,
                    contactsToImport = emptyList(),
                    contactsAlreadyImported = emptyList(),
                ),
                onEvent = {},
                onClose = {},
            )
        }
    }
}

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_CONTACTS_TO_IMPORT_TITLE = "sim_import_contacts_to_import_title"

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_IMPORT_BUTTON = "sim_import_import_button"

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_SELECT_ALL = "sim_import_select_all"

@VisibleForTesting
const val TEST_TAG_SIM_IMPORT_DESELECT_ALL = "sim_import_deselect_all"
