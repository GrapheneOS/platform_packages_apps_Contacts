package com.android.contacts.ui.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.settings.common.SETTINGS_BOTTOM_PADDING
import com.android.contacts.ui.settings.common.SETTINGS_CELL_SPACING
import com.android.contacts.ui.settings.common.SETTINGS_GROUP_SPACING
import com.android.contacts.ui.settings.common.SETTINGS_HORIZONTAL_PADDING
import com.android.contacts.ui.settings.common.SettingsCell
import com.android.contacts.ui.settings.common.SettingsSectionHeader
import com.android.contacts.ui.settings.common.SettingsSingleChoiceDialog
import com.android.contacts.ui.settings.common.SettingsTopAppBar
import com.android.contacts.ui.settings.screen.model.SETTINGS_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_ITEM_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_SECTION_HEADER_TEST_TAG_PREFIX
import com.android.contacts.ui.settings.screen.model.SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG
import com.android.contacts.ui.settings.screen.model.SETTINGS_SNACKBAR_TEST_TAG
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsChoice
import com.android.contacts.ui.settings.screen.model.SettingsGroupId
import com.android.contacts.ui.settings.screen.model.SettingsGroupUiModel
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsItemUiModel
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SettingsMainScreen(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var openedDialog by rememberSaveable { mutableStateOf<SettingsItemId?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            SettingsTopAppBar(
                title = stringResource(R.string.activity_title_settings),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag(SETTINGS_SNACKBAR_TEST_TAG),
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        SettingsGroups(
            groups = uiState.groups,
            contentPadding = contentPadding,
            onItemClick = { id ->
                when (id) {
                    SettingsItemId.SORT_ORDER,
                    SettingsItemId.DISPLAY_ORDER,
                    SettingsItemId.PHONETIC_NAME_DISPLAY,
                    -> openedDialog = id

                    SettingsItemId.ABOUT -> onNavigateToAbout()
                    else -> onAction(Action.ItemClicked(id))
                }
            },
        )
    }

    SettingsDialogs(
        openedDialog = openedDialog,
        uiState = uiState,
        onAction = onAction,
        onDismissRequest = { openedDialog = null },
    )
}

@Composable
private fun SettingsGroups(
    groups: ImmutableList<SettingsGroupUiModel>,
    contentPadding: PaddingValues,
    onItemClick: (SettingsItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = SETTINGS_HORIZONTAL_PADDING +
                contentPadding.calculateStartPadding(layoutDirection),
            end = SETTINGS_HORIZONTAL_PADDING +
                contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            bottom = SETTINGS_BOTTOM_PADDING + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(SETTINGS_GROUP_SPACING),
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = groups,
            key = { group -> group.id },
        ) { group ->
            SettingsGroup(
                group = group,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    group: SettingsGroupUiModel,
    onItemClick: (SettingsItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag(SETTINGS_GROUP_TEST_TAG_PREFIX + group.id.name),
    ) {
        if (group.title != null) {
            val headerTestTag = SETTINGS_SECTION_HEADER_TEST_TAG_PREFIX + group.id.name

            SettingsSectionHeader(
                title = group.title,
                modifier = Modifier.testTag(headerTestTag),
            )
        }

        SettingsGroupCells(
            group = group,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun SettingsGroupCells(
    group: SettingsGroupUiModel,
    onItemClick: (SettingsItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SETTINGS_CELL_SPACING),
        modifier = modifier,
    ) {
        group.items.forEachIndexed { index, item ->
            SettingsCell(
                title = item.title,
                summary = item.summary,
                isFirst = index == 0,
                isLast = index == group.items.lastIndex,
                onClick = { onItemClick(item.id) },
                modifier = Modifier.testTag(SETTINGS_ITEM_TEST_TAG_PREFIX + item.id.name),
            )
        }
    }
}

@Composable
private fun SettingsDialogs(
    openedDialog: SettingsItemId?,
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (openedDialog) {
        SettingsItemId.SORT_ORDER -> {
            SettingsSingleChoiceDialog(
                title = stringResource(R.string.display_options_sort_list_by),
                options = sortOrderChoices(),
                selected = uiState.sortOrder,
                onSelect = { sortOrder ->
                    onAction(Action.SortOrderSelected(sortOrder))
                    onDismissRequest()
                },
                onDismissRequest = onDismissRequest,
                modifier = Modifier.testTag(SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG),
            )
        }

        SettingsItemId.DISPLAY_ORDER -> {
            SettingsSingleChoiceDialog(
                title = stringResource(R.string.display_options_view_names_as),
                options = displayOrderChoices(),
                selected = uiState.displayOrder,
                onSelect = { displayOrder ->
                    onAction(Action.DisplayOrderSelected(displayOrder))
                    onDismissRequest()
                },
                onDismissRequest = onDismissRequest,
                modifier = Modifier.testTag(SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG),
            )
        }

        SettingsItemId.PHONETIC_NAME_DISPLAY -> {
            SettingsSingleChoiceDialog(
                title = stringResource(R.string.display_options_phonetic_name_fields),
                options = phoneticNameDisplayChoices(),
                selected = uiState.phoneticNameDisplay,
                onSelect = { phoneticNameDisplay ->
                    onAction(Action.PhoneticNameDisplaySelected(phoneticNameDisplay))
                    onDismissRequest()
                },
                onDismissRequest = onDismissRequest,
                modifier = Modifier.testTag(SETTINGS_SINGLE_CHOICE_DIALOG_TEST_TAG),
            )
        }

        else -> Unit
    }
}

@Composable
private fun sortOrderChoices(): ImmutableList<SettingsChoice<SortOrder>> {
    return persistentListOf(
        SettingsChoice(
            value = SortOrder.GIVEN_NAME_FIRST,
            label = stringResource(R.string.display_options_sort_by_given_name),
        ),
        SettingsChoice(
            value = SortOrder.FAMILY_NAME_FIRST,
            label = stringResource(R.string.display_options_sort_by_family_name),
        ),
    )
}

@Composable
private fun displayOrderChoices(): ImmutableList<SettingsChoice<DisplayOrder>> {
    return persistentListOf(
        SettingsChoice(
            value = DisplayOrder.GIVEN_NAME_FIRST,
            label = stringResource(R.string.display_options_view_given_name_first),
        ),
        SettingsChoice(
            value = DisplayOrder.FAMILY_NAME_FIRST,
            label = stringResource(R.string.display_options_view_family_name_first),
        ),
    )
}

@Composable
private fun phoneticNameDisplayChoices(): ImmutableList<SettingsChoice<PhoneticNameDisplay>> {
    return persistentListOf(
        SettingsChoice(
            value = PhoneticNameDisplay.SHOW_ALWAYS,
            label = stringResource(R.string.editor_options_always_show_phonetic_names),
        ),
        SettingsChoice(
            value = PhoneticNameDisplay.HIDE_IF_EMPTY,
            label = stringResource(R.string.editor_options_hide_phonetic_names_if_empty),
        ),
    )
}

@PreviewLightDark
@Composable
private fun SettingsMainScreenPreview() {
    ContactsPreviewTheme {
        SettingsMainScreen(
            uiState = SettingsUiState(
                groups = persistentListOf(
                    SettingsGroupUiModel(
                        id = SettingsGroupId.PROFILE,
                        items = persistentListOf(
                            SettingsItemUiModel(
                                id = SettingsItemId.MY_INFO,
                                title = "My info",
                                summary = "Anna Smith",
                            ),
                        ),
                    ),
                    SettingsGroupUiModel(
                        id = SettingsGroupId.ACCOUNTS,
                        items = persistentListOf(
                            SettingsItemUiModel(
                                id = SettingsItemId.ACCOUNTS,
                                title = "Accounts",
                            ),
                            SettingsItemUiModel(
                                id = SettingsItemId.DEFAULT_ACCOUNT,
                                title = "Default account for new contacts",
                                summary = "Device",
                            ),
                            SettingsItemUiModel(
                                id = SettingsItemId.CONTACTS_FILTER,
                                title = "Contacts to display",
                                summary = "All contacts",
                            ),
                        ),
                    ),
                    SettingsGroupUiModel(
                        id = SettingsGroupId.DISPLAY,
                        title = "Display options",
                        items = persistentListOf(
                            SettingsItemUiModel(
                                id = SettingsItemId.SORT_ORDER,
                                title = "Sort by",
                                summary = "First name",
                            ),
                            SettingsItemUiModel(
                                id = SettingsItemId.DISPLAY_ORDER,
                                title = "Name format",
                                summary = "First name first",
                            ),
                        ),
                    ),
                ),
                sortOrder = SortOrder.GIVEN_NAME_FIRST,
                displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
                phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            ),
            onAction = {},
            onNavigateBack = {},
            onNavigateToAbout = {},
        )
    }
}
