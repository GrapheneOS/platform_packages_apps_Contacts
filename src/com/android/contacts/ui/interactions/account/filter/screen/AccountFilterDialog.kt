@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.interactions.account.filter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.ui.common.components.AccountIcon
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_ITEM_ACCOUNT_TEST_TAG_PREFIX
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_ITEM_ALL_TEST_TAG
import com.android.contacts.ui.interactions.account.filter.screen.model.ACCOUNT_FILTER_LOADING_TEST_TAG
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterAction as Action
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterUiState as State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun AccountFilterDialog(
    effectHandler: AccountFilterEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: AccountFilterScreenModel = viewModel<AccountFilterViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    AccountFilterDialogContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun AccountFilterDialogContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(Action.Dismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
        content = {
            when (uiState) {
                is State.Loading -> {
                    AcountFiltersLoading()
                }

                is State.Ready -> {
                    FilterItemList(
                        items = uiState.items,
                        onClick = { onAction(Action.ItemClicked(it)) },
                    )
                }
            }
        },
    )
}

@Composable
private fun AcountFiltersLoading() {
    Box(Modifier.fillMaxWidth()) {
        CircularProgressIndicator(
            Modifier
                .align(Alignment.Center)
                .padding(40.dp)
                .size(32.dp)
                .testTag(ACCOUNT_FILTER_LOADING_TEST_TAG),
        )
    }
}

@Composable
private fun FilterItemList(
    items: ImmutableList<SelectableItem<AccountFilterItem>>,
    onClick: (AccountFilterItem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(
            items = items,
            key = { item -> item.item.filter },
        ) { item ->
            FilterCell(
                item = item.item,
                isSelected = item.isSelected,
                onClick = { onClick(item.item) },
            )
        }
    }
}

@Composable
private fun FilterCell(
    item: AccountFilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .testTag(filterCellTestTag(item)),
    ) {
        FilterCellIcon(item)

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
        ) {
            Text(
                text = when (item) {
                    is AccountFilterItem.All -> stringResource(R.string.local_search_label)
                    is AccountFilterItem.One -> item.name
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            if (item is AccountFilterItem.One && item.description != null) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Text(
            text = item.contactsCount.toString(),
            style = MaterialTheme.typography.labelMedium,
        )

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp)
                .visible(isSelected)
        )
    }
}

@Composable
private fun FilterCellIcon(item: AccountFilterItem) {
    when (item) {
        is AccountFilterItem.All -> {
            Icon(
                imageVector = Icons.Outlined.PeopleAlt,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }
        is AccountFilterItem.One -> {
            AccountIcon(
                iconData = item.iconData,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun filterCellTestTag(item: AccountFilterItem): String {
    return when (item) {
        is AccountFilterItem.All ->
            ACCOUNT_FILTER_ITEM_ALL_TEST_TAG
        is AccountFilterItem.One ->
            ACCOUNT_FILTER_ITEM_ACCOUNT_TEST_TAG_PREFIX + item.name
    }
}

@PreviewLightDark
@Composable
private fun AccountFilterDialogProgressPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            AccountFilterDialogContent(
                uiState = State.Loading,
                onAction = {},
                sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AccountFilterDialogListPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            AccountFilterDialogContent(
                uiState = State.Ready(items = previewItems()),
                onAction = {},
                sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
            )
        }
    }
}

private fun previewItems(): PersistentList<SelectableItem<AccountFilterItem>> {
    val itemAll = AccountFilterItem.All(
        contactsCount = 100,
    )
    val account1 = AccountModel(name = "user@example.org")
    val account2 = AccountModel(name = "another@example.org")
    val itemAccount1 = AccountFilterItem.One(
        name = account1.name!!,
        description = "Google",
        iconData = AccountIconData(iconRes = R.drawable.quantum_ic_smartphone_vd_theme_24),
        contactsCount = 60,
        filter = ContactsAccountFilter.One(account1),
    )
    val itemAccount2 = AccountFilterItem.One(
        name = account2.name!!,
        description = "Device",
        iconData = AccountIconData(iconRes = R.drawable.quantum_ic_smartphone_vd_theme_24),
        contactsCount = 40,
        filter = ContactsAccountFilter.One(account2),
    )
    return persistentListOf(
        SelectableItem(itemAll, isSelected = true),
        SelectableItem(itemAccount1, isSelected = false),
        SelectableItem(itemAccount2, isSelected = false),
    )
}
