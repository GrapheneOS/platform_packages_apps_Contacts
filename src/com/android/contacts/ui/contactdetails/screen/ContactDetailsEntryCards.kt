package com.android.contacts.ui.contactdetails.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsConnectedAppRow
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.common.ContactEntryCard
import com.android.contacts.ui.contactdetails.common.ContactEntryRow
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactConnectedAppUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContactDetailsEntryCard(
    groups: ImmutableList<ContactEntryGroupUiModel>,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    isTopRounded: Boolean = true,
) {
    ContactEntryCard(
        groups = groups,
        isTopRounded = isTopRounded,
        onEntryClick = { entry ->
            entry.action?.let { action -> onAction(Action.EntryClick(action)) }
        },
        onEntryCopyClick = { entry ->
            entry.copyText?.let { text -> onAction(Action.CopyClick(entry.copyLabel, text)) }
        },
        onEntrySetDefaultClick = { entry -> onAction(Action.SetDefaultClick(entry.id)) },
        onEntryClearDefaultClick = { entry -> onAction(Action.ClearDefaultClick(entry.id)) },
        onEntryCallingSimClick = { onAction(Action.CallingSimClick) },
        onEntryActionClick = { action -> onAction(Action.EntryClick(action)) },
        modifier = modifier,
    )
}

@Composable
internal fun ContactDetailsConnectedApps(
    connectedApps: ImmutableList<ContactConnectedAppUiModel>,
    expandedPackages: Set<String>,
    onAppClick: (String) -> Unit,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier,
    ) {
        connectedApps.forEachIndexed { index, connectedApp ->
            val isExpanded = connectedApp.packageName in expandedPackages

            ContactDetailsConnectedAppRow(
                connectedApp = connectedApp,
                isExpanded = isExpanded,
                isLast = !isExpanded && index == connectedApps.lastIndex,
                onClick = { onAppClick(connectedApp.packageName) },
            )

            if (isExpanded) {
                val groups = remember(connectedApp.entries) {
                    persistentListOf(ContactEntryGroupUiModel(entries = connectedApp.entries))
                }

                ContactDetailsEntryCard(
                    groups = groups,
                    onAction = onAction,
                    isTopRounded = false,
                )
            }
        }
    }
}

@Composable
internal fun ContactDetailsEmptyPrompt(
    prompt: ContactDetailsEmptyPromptUiModel,
    onEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier.testTag(CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG),
    ) {
        prompt.entries.forEachIndexed { index, entry ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = cellShape(
                    isFirst = index == 0,
                    isLast = index == prompt.entries.lastIndex,
                ),
            ) {
                ContactEntryRow(
                    entry = entry,
                    isIconVisible = true,
                    onClick = onEntryClick,
                    onCopyClick = {},
                    onSetDefaultClick = {},
                    onClearDefaultClick = {},
                    onCallingSimClick = {},
                    onEntryActionClick = {},
                )
            }
        }
    }
}
