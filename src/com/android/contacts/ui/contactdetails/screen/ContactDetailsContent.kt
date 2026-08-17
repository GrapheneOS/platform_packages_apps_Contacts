package com.android.contacts.ui.contactdetails.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsActionRow
import com.android.contacts.ui.contactdetails.common.ContactDetailsHeader
import com.android.contacts.ui.contactdetails.common.ContactDetailsProgressDialog
import com.android.contacts.ui.contactdetails.common.ContactDetailsQuickActions
import com.android.contacts.ui.contactdetails.common.ContactDetailsTopAppBar
import com.android.contacts.ui.contactdetails.common.measuredInto
import com.android.contacts.ui.contactdetails.common.rememberOverlayHeight
import com.android.contacts.ui.contactdetails.common.ContactEntryCard
import com.android.contacts.ui.contactdetails.common.ContactEntryRow
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ABOUT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_RINGTONE_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import com.android.contacts.ui.core.ContactsPreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State

private const val VISIBLE_ALPHA = 1f
private const val HIDDEN_ALPHA = 0f
private const val HEADER_KEY = "header"
private const val QUICK_ACTIONS_KEY = "quick_actions"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactDetailsContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState.content
    val loaded = content as? Content.Loaded
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val isHeaderScrolledAway by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    Scaffold(
        topBar = {
            ContactDetailsTopAppBar(
                title = loaded?.header?.displayName.orEmpty(),
                isTitleVisible = loaded != null && isHeaderScrolledAway,
                menu = loaded?.menu,
                isStarred = loaded?.isStarred == true,
                onAction = onAction,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { contentPadding ->
        when (content) {
            is Content.Loading -> {
                ContactDetailsPlaceholder(contentPadding) {
                    CircularProgressIndicator()
                }
            }

            is Content.NotFound, is Content.Error -> {
                ContactDetailsPlaceholder(contentPadding) {
                    ContactDetailsMessage(textResource = R.string.invalidContactMessage)
                }
            }

            is Content.Loaded -> {
                ContactDetailsCards(
                    content = content,
                    onAction = onAction,
                    contentPadding = contentPadding,
                    listState = listState,
                )
            }
        }
    }

    val linkProgress = uiState.linkProgress
    if (linkProgress != null) {
        ContactDetailsProgressDialog(operation = linkProgress)
    }
}

@Composable
private fun ContactDetailsMessage(
    @StringRes textResource: Int,
) {
    Text(
        text = stringResource(textResource),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ContactDetailsPlaceholder(
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = Tokens.screenHorizontalPadding),
    ) {
        content()
    }
}

@Composable
private fun ContactDetailsCards(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val headerHeight = rememberOverlayHeight()
    val quickActionsHeight = rememberOverlayHeight()
    val isListLaidOut by remember(listState) {
        derivedStateOf { listState.layoutInfo.totalItemsCount > 0 }
    }
    val overlayAlpha = when {
        isListLaidOut -> VISIBLE_ALPHA
        else -> HIDDEN_ALPHA
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .clipToBounds(),
    ) {
        if (headerHeight.isMeasured && quickActionsHeight.isMeasured) {
            ContactDetailsList(
                content = content,
                onAction = onAction,
                contentPadding = contentPadding,
                listState = listState,
                headerHeight = headerHeight.value,
                quickActionsHeight = quickActionsHeight.value,
            )
        }

        ContactDetailsHeader(
            header = content.header,
            modifier = Modifier
                .alpha(overlayAlpha)
                .offset {
                    IntOffset(
                        x = 0,
                        y = listState.headerOffset(headerHeight.value.roundToPx()),
                    )
                }
                .measuredInto(headerHeight)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontalContentPadding(contentPadding)),
        )

        ContactDetailsQuickActions(
            quickActions = content.quickActions,
            onActionClick = { action -> onAction(Action.EntryClick(action)) },
            modifier = Modifier
                .alpha(overlayAlpha)
                .offset {
                    IntOffset(
                        x = 0,
                        y = listState.quickActionsOffset(),
                    )
                }
                .measuredInto(quickActionsHeight)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontalContentPadding(contentPadding))
                .padding(bottom = Tokens.quickActionPinnedPadding),
        )
    }
}

@Composable
private fun ContactDetailsList(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    headerHeight: Dp,
    quickActionsHeight: Dp,
) {
    LazyColumn(
        state = listState,
        contentPadding = screenContentPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Tokens.cardGroupSpacing),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = HEADER_KEY) {
            Spacer(modifier = Modifier.height(headerHeight))
        }

        item(key = QUICK_ACTIONS_KEY) {
            Spacer(modifier = Modifier.height(quickActionsHeight))
        }

        if (content.contactCard.isNotEmpty()) {
            item(key = "contact_card") {
                ContactDetailsEntryCard(
                    groups = content.contactCard,
                    onAction = onAction,
                    modifier = Modifier.testTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG),
                )
            }
        }

        if (content.aboutCard.isNotEmpty()) {
            item(key = "about_card") {
                ContactDetailsAboutCard(
                    title = content.aboutCardTitle,
                    groups = content.aboutCard,
                    onAction = onAction,
                )
            }
        }

        val emptyPrompt = content.emptyPrompt
        if (emptyPrompt != null) {
            item(key = "empty_prompt") {
                ContactDetailsEmptyPrompt(
                    prompt = emptyPrompt,
                    onEntryClick = { onAction(Action.AddDetailsClick) },
                )
            }
        }

        if (content.menu.isRingtoneVisible) {
            item(key = "ringtone") {
                ContactDetailsActionRow(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.menu_set_ring_tone),
                    onClick = { onAction(Action.RingtoneClick) },
                    modifier = Modifier.testTag(CONTACT_DETAILS_RINGTONE_TEST_TAG),
                )
            }
        }
    }
}


private fun LazyListState.headerOffset(headerHeight: Int): Int {
    return itemOffset(key = HEADER_KEY, index = 0, scrolledPastOffset = -headerHeight)
}

private fun LazyListState.quickActionsOffset(): Int {
    return itemOffset(key = QUICK_ACTIONS_KEY, index = 1, scrolledPastOffset = 0)
        .coerceAtLeast(0)
}

private fun LazyListState.itemOffset(
    key: String,
    index: Int,
    scrolledPastOffset: Int,
): Int {
    val info = layoutInfo.visibleItemsInfo.firstOrNull { item -> item.key == key }

    return when {
        info != null -> info.offset
        firstVisibleItemIndex > index -> scrolledPastOffset
        else -> layoutInfo.viewportEndOffset
    }
}

@Composable
private fun horizontalContentPadding(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = Tokens.screenHorizontalPadding +
            contentPadding.calculateStartPadding(layoutDirection),
        end = Tokens.screenHorizontalPadding +
            contentPadding.calculateEndPadding(layoutDirection),
    )
}

@Composable
private fun ContactDetailsAboutCard(
    title: String,
    groups: ImmutableList<ContactEntryGroupUiModel>,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ContactDetailsSectionHeader(title = title)

        ContactDetailsEntryCard(
            groups = groups,
            onAction = onAction,
            modifier = Modifier.testTag(CONTACT_DETAILS_ABOUT_CARD_TEST_TAG),
        )
    }
}

@Composable
private fun ContactDetailsEntryCard(
    groups: ImmutableList<ContactEntryGroupUiModel>,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    ContactEntryCard(
        groups = groups,
        onEntryClick = { entry ->
            entry.action?.let { action -> onAction(Action.EntryClick(action)) }
        },
        onEntryCopyClick = { entry ->
            entry.copyText?.let { text -> onAction(Action.CopyClick(entry.copyLabel, text)) }
        },
        onEntrySetDefaultClick = { entry -> onAction(Action.SetDefaultClick(entry.id)) },
        onEntryClearDefaultClick = { entry -> onAction(Action.ClearDefaultClick(entry.id)) },
        onAlternateActionClick = { action -> onAction(Action.EntryClick(action)) },
        onThirdActionClick = { action -> onAction(Action.EntryClick(action)) },
        modifier = modifier,
    )
}

@Composable
private fun ContactDetailsEmptyPrompt(
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
                    onAlternateActionClick = {},
                    onThirdActionClick = {},
                )
            }
        }
    }
}

@Composable
private fun ContactDetailsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = Tokens.sectionHeaderPadding,
            end = Tokens.sectionHeaderPadding,
            bottom = Tokens.sectionHeaderPadding,
        ),
    )
}

@Composable
private fun screenContentPadding(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = Tokens.screenHorizontalPadding +
            contentPadding.calculateStartPadding(layoutDirection),
        end = Tokens.screenHorizontalPadding +
            contentPadding.calculateEndPadding(layoutDirection),
        bottom = Tokens.screenBottomPadding +
            contentPadding.calculateBottomPadding(),
    )
}

@PreviewLightDark
@Composable
private fun ContactDetailsContentPreview() {
    ContactsPreviewTheme {
        ContactDetailsContent(
            uiState = State(content = previewContent()),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsContentLoadingPreview() {
    ContactsPreviewTheme {
        ContactDetailsContent(
            uiState = State(content = Content.Loading),
            onAction = {},
        )
    }
}

private fun previewContent(): Content.Loaded {
    return Content.Loaded(
        header = ContactHeaderUiModel(
            displayName = "Anna Smith",
            subtitles = persistentListOf("Annie", "Acme"),
            photo = null,
            avatarSeed = "anna-smith",
            isBusiness = false,
            isDisplayNameLtr = false,
        ),
        quickActions = persistentListOf(
            previewQuickAction(ContactEntryIcon.CALL, "Call", ContactEntryAction.Call("088")),
            previewQuickAction(ContactEntryIcon.MESSAGE, "Text", ContactEntryAction.Sms("088")),
            previewQuickAction(ContactEntryIcon.VIDEO_CALL, "Video", null),
            previewQuickAction(ContactEntryIcon.EMAIL, "Email", null),
        ),
        contactCard = persistentListOf(
            ContactEntryGroupUiModel(
                entries = persistentListOf(
                    previewEntry(id = 1L, header = "088 525 7470", text = "Mobile"),
                ),
            ),
        ),
        aboutCard = persistentListOf(
            ContactEntryGroupUiModel(
                entries = persistentListOf(
                    previewEntry(
                        id = 2L,
                        header = "Met at the conference",
                        text = "Note",
                        icon = null,
                    ),
                ),
            ),
        ),
        aboutCardTitle = "About Anna",
        emptyPrompt = null,
        menu = ContactDetailsMenu(
            isStarVisible = true,
            editAction = ContactDetailsEditAction.EDIT,
            isJoinVisible = true,
            isLinkedContactsVisible = false,
            isDeleteVisible = true,
            isShareVisible = true,
            isShortcutVisible = true,
            isRingtoneVisible = true,
        ),
        isStarred = false,
    )
}

private fun previewQuickAction(
    icon: ContactEntryIcon,
    label: String,
    action: ContactEntryAction?,
): ContactQuickActionUiModel {
    return ContactQuickActionUiModel(
        icon = icon,
        label = label,
        action = action,
    )
}

private fun previewEntry(
    id: Long,
    header: String,
    text: String,
    icon: ContactEntryIcon? = ContactEntryIcon.CALL,
): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        icon = icon,
        header = header,
        isHeaderLtr = false,
        subHeader = null,
        text = text,
        isSuperPrimary = false,
        isDefaultChangeable = false,
        action = null,
        alternateAction = null,
        thirdAction = null,
        copyText = header,
        copyLabel = text,
    )
}
