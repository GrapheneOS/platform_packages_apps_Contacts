@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.group.list.screen

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.core.ContactsPreviewDialog
import com.android.contacts.ui.group.list.screen.model.AccountGroupsItem
import com.android.contacts.ui.group.list.screen.model.GROUPS_ACCOUNT_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_LOADING_TEST_TAG
import com.android.contacts.ui.group.list.screen.model.GROUPS_NEW_GROUP_TEST_TAG_PREFIX
import com.android.contacts.ui.group.list.screen.model.GROUPS_NO_ACCOUNTS_TEST_TAG
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import com.android.contacts.ui.group.list.screen.model.GroupsAction as Action
import com.android.contacts.ui.group.list.screen.model.GroupsUiState as State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize

@Composable
internal fun GroupsScreen(
    effectHandler: GroupsEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: GroupsScreenModel = viewModel<GroupsViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    GroupsContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun GroupsContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(Action.Dismissed) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
        content = {
            when (uiState) {
                State.Loading -> {
                    GroupsLoading()
                }
                State.WithoutAccounts -> {
                    NoAccountsMessage()
                }
                is State.WithAccounts -> {
                    GroupsList(
                        groups = uiState.groups,
                        showAccountHeaders = uiState.showAccountHeaders,
                        onNewClick = { onAction(Action.NewClicked(it)) },
                        onGroupClick = { onAction(Action.GroupClicked(it)) },
                    )
                }
            }
        },
    )
}

@Composable
private fun GroupsLoading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .testTag(GROUPS_LOADING_TEST_TAG)
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoAccountsMessage() {
    Text(
        text = stringResource(R.string.groups_empty_accounts_message),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .testTag(GROUPS_NO_ACCOUNTS_TEST_TAG)
            .fillMaxWidth()
            .padding(32.dp),
    )
}

@Composable
private fun GroupsList(
    groups: ImmutableList<AccountGroupsItem>,
    showAccountHeaders: Boolean,
    onNewClick: (AccountModel?) -> Unit,
    onGroupClick: (GroupUiItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(bottom = 16.dp),
    ) {
        groups.forEach { accountGroup ->
            if (showAccountHeaders) {
                item(accountGroup.account) {
                    AccountHeader(accountGroup.accountName)
                }
            }
            if (accountGroup.canCreateGroup) {
                item(NewGroupKey(accountGroup.account)) {
                    NewGroupCell(accountGroup, onNewClick)
                }
            }
            items(accountGroup.groups, { it.id }) { group ->
                GroupCell(group, onGroupClick)
            }
        }
    }
}

@Composable
private fun AccountHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .testTag(GROUPS_ACCOUNT_TEST_TAG_PREFIX + name)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun NewGroupCell(
    accountGroups: AccountGroupsItem,
    onNewClick: (AccountModel?) -> Unit,
) {
    val resources = LocalResources.current
    TextButton(
        onClick = { onNewClick(accountGroups.account) },
        shape = RectangleShape,
        modifier = Modifier
            .testTag(GROUPS_NEW_GROUP_TEST_TAG_PREFIX + accountGroups.account?.name)
            .fillMaxWidth()
            .semantics {
                if (accountGroups.account != null) {
                    contentDescription = resources.getString(
                        R.string.new_group_content_description,
                        accountGroups.accountName,
                    )
                }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
            )
            Text(
                text = stringResource(R.string.menu_new_group_action_bar),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun GroupCell(
    group: GroupUiItem,
    onGroupClick: (GroupUiItem) -> Unit,
) {
    TextButton(
        onClick = { onGroupClick(group) },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RectangleShape,
        modifier = Modifier.testTag(GROUPS_GROUP_TEST_TAG_PREFIX + group.id),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Label,
                contentDescription = null,
            )
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            )
            Text(
                text = group.summaryCount.toString(),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Parcelize
private data class NewGroupKey(
    val account: AccountModel?,
) : Parcelable

@PreviewLightDark
@Composable
private fun GroupsScreenLoadingPreview() {
    ContactsPreviewDialog {
        GroupsContent(
            uiState = State.Loading,
            onAction = {},
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
        )
    }
}

@PreviewLightDark
@Composable
private fun GroupsScreenWithoutAccountsPreview() {
    ContactsPreviewDialog {
        GroupsContent(
            uiState = State.WithoutAccounts,
            onAction = {},
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
        )
    }
}

@PreviewLightDark
@Composable
private fun GroupsScreenWithAccountsPreview() {
    val account1 = AccountModel(name = "user@example.org")
    val account2 = AccountModel(name = "another@example.org")
    val item1 = AccountGroupsItem(
        account = account1,
        accountName = account1.name!!,
        groups = persistentListOf(
            GroupUiItem(1L, "Favorites", 3),
            GroupUiItem(2L, "Friends", 21),
        ),
        canCreateGroup = true,
    )
    val item2 = AccountGroupsItem(
        account = account2,
        accountName = account2.name!!,
        groups = persistentListOf(
            GroupUiItem(3L, "Work Collegues", 8),
        ),
        canCreateGroup = false,
    )
    ContactsPreviewDialog {
        GroupsContent(
            uiState = State.WithAccounts(
                groups = persistentListOf(item1, item2),
                showAccountHeaders = true,
            ),
            onAction = {},
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
        )
    }
}
