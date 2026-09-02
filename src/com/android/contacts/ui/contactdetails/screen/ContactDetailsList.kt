package com.android.contacts.ui.contactdetails.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.android.contacts.R
import com.android.contacts.ui.contactdetails.common.ContactDetailsGroups
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ACCOUNTS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_NOTES_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_RECENT_CALLS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTINGS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content

private const val GROUPS_KEY = "groups"
private const val CONTACT_CARD_KEY = "contact_card"
private const val NOTES_KEY = "notes"
private const val EMPTY_PROMPT_KEY = "empty_prompt"
private const val RECENT_CALLS_KEY = "recent_calls"
private const val CONNECTED_APPS_KEY = "connected_apps"
private const val SETTINGS_KEY = "settings"
private const val ACCOUNTS_KEY = "accounts"

@Composable
internal fun ContactDetailsList(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    headerHeight: Int,
    quickActionsHeight: Int,
) {
    val itemPadding = horizontalContentPadding(contentPadding)
    val density = LocalDensity.current

    val headerSpacing = with(density) {
        headerHeight.toDp()
    } + Tokens.cardGroupSpacing

    val quickActionsSpacing = with(density) {
        quickActionsHeight.toDp()
    } + groupsSpacing(content.groups.isNotEmpty())

    var expandedConnectedApps by rememberSaveable { mutableStateOf(emptySet<String>()) }

    LazyColumn(
        state = listState,
        contentPadding = screenContentPadding(contentPadding),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = HEADER_KEY) {
            Spacer(modifier = Modifier.height(headerSpacing))
        }

        item(key = QUICK_ACTIONS_KEY) {
            Spacer(modifier = Modifier.height(quickActionsSpacing))
        }

        groupsItem(content, onAction, itemPadding)
        contactCardItem(content, onAction, itemPadding)
        notesSection(content, onAction, itemPadding)
        emptyPromptItem(content, onAction, itemPadding)
        recentCallsSection(content, onAction, itemPadding)
        connectedAppsSection(
            content = content,
            onAction = onAction,
            itemPadding = itemPadding,
            expandedPackages = expandedConnectedApps,
            onExpandedPackagesChanged = { packages -> expandedConnectedApps = packages },
        )
        settingsSection(content, onAction, itemPadding)
        accountsItem(content, itemPadding)
    }
}

private fun LazyListScope.groupsItem(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    if (content.groups.isEmpty()) {
        return
    }

    item(key = GROUPS_KEY) {
        ContactDetailsGroups(
            groups = content.groups,
            onGroupClick = { groupId -> onAction(Action.GroupClick(groupId)) },
            contentPadding = itemPadding,
            modifier = Modifier.padding(bottom = Tokens.groupChipSectionSpacing),
        )
    }
}

private fun LazyListScope.contactCardItem(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    if (content.contactCard.isEmpty()) {
        return
    }

    item(key = CONTACT_CARD_KEY) {
        ContactDetailsEntryCard(
            groups = content.contactCard,
            onAction = onAction,
            modifier = Modifier
                .padding(itemPadding)
                .testTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG)
                .padding(bottom = Tokens.cardGroupSpacing),
        )
    }
}

private fun LazyListScope.notesSection(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    if (content.notes.isEmpty()) {
        return
    }

    cardSection(
        key = NOTES_KEY,
        titleResource = R.string.label_notes,
        testTag = CONTACT_DETAILS_NOTES_TEST_TAG,
        itemPadding = itemPadding,
    ) {
        ContactDetailsEntryCard(
            groups = content.notes,
            onAction = onAction,
            isTopRounded = false,
        )
    }
}

private fun LazyListScope.emptyPromptItem(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    val emptyPrompt = content.emptyPrompt ?: return

    item(key = EMPTY_PROMPT_KEY) {
        ContactDetailsEmptyPrompt(
            prompt = emptyPrompt,
            onEntryClick = { onAction(Action.AddDetailsClick) },
            modifier = Modifier
                .padding(itemPadding)
                .padding(bottom = Tokens.cardGroupSpacing),
        )
    }
}

private fun LazyListScope.recentCallsSection(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    if (content.recentCalls.isEmpty()) {
        return
    }

    cardSection(
        key = RECENT_CALLS_KEY,
        titleResource = R.string.contact_details_recent_activity,
        testTag = CONTACT_DETAILS_RECENT_CALLS_TEST_TAG,
        itemPadding = itemPadding,
    ) {
        ContactDetailsRecentCalls(
            recentCalls = content.recentCalls,
            onRecentCallClick = { onAction(Action.RecentCallClick) },
        )
    }
}

private fun LazyListScope.connectedAppsSection(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
    expandedPackages: Set<String>,
    onExpandedPackagesChanged: (Set<String>) -> Unit,
) {
    if (content.connectedApps.isEmpty()) {
        return
    }

    cardSection(
        key = CONNECTED_APPS_KEY,
        titleResource = R.string.contact_details_connected_apps,
        testTag = CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG,
        itemPadding = itemPadding,
    ) {
        ContactDetailsConnectedApps(
            connectedApps = content.connectedApps,
            expandedPackages = expandedPackages,
            onAppClick = { packageName ->
                onExpandedPackagesChanged(
                    when (packageName) {
                        in expandedPackages -> expandedPackages - packageName
                        else -> expandedPackages + packageName
                    },
                )
            },
            onAction = onAction,
        )
    }
}

private fun LazyListScope.settingsSection(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    itemPadding: PaddingValues,
) {
    if (content.settings.isEmpty()) {
        return
    }

    cardSection(
        key = SETTINGS_KEY,
        titleResource = R.string.contact_details_settings_title,
        testTag = CONTACT_DETAILS_SETTINGS_TEST_TAG,
        itemPadding = itemPadding,
    ) {
        ContactDetailsSettings(
            settings = content.settings,
            onAction = onAction,
        )
    }
}

private fun LazyListScope.accountsItem(
    content: Content.Loaded,
    itemPadding: PaddingValues,
) {
    if (content.accounts.isEmpty()) {
        return
    }

    item(key = ACCOUNTS_KEY) {
        ContactDetailsAccounts(
            accounts = content.accounts,
            modifier = Modifier
                .padding(itemPadding)
                .testTag(CONTACT_DETAILS_ACCOUNTS_TEST_TAG)
                .padding(bottom = Tokens.cardGroupSpacing),
        )
    }
}

private fun LazyListScope.cardSection(
    key: String,
    @StringRes titleResource: Int,
    testTag: String,
    itemPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    item(key = key) {
        ContactDetailsSection(
            title = stringResource(titleResource),
            modifier = Modifier
                .padding(itemPadding)
                .testTag(testTag)
                .padding(bottom = Tokens.cardGroupSpacing),
        ) {
            content()
        }
    }
}

private fun groupsSpacing(hasGroups: Boolean): Dp {
    return when {
        hasGroups -> Tokens.groupChipSectionSpacing
        else -> Tokens.cardGroupSpacing
    }
}
