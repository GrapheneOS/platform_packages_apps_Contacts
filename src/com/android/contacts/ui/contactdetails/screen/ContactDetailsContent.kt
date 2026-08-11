package com.android.contacts.ui.contactdetails.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsHeader
import com.android.contacts.ui.contactdetails.common.ContactDetailsProgressDialog
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens
import com.android.contacts.ui.contactdetails.common.ContactDetailsTopAppBar
import com.android.contacts.ui.contactdetails.common.ContactEntryCard
import com.android.contacts.ui.contactdetails.common.ContactEntryRow
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ABOUT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.core.ContactsPreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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

    Scaffold(
        topBar = {
            ContactDetailsTopAppBar(
                menu = loaded?.menu,
                isStarred = loaded?.isStarred == true,
                onAction = onAction,
                scrollBehavior = scrollBehavior,
            )
        },
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
            .padding(horizontal = ContactDetailsTokens.screenHorizontalPadding),
    ) {
        content()
    }
}

@Composable
private fun ContactDetailsCards(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = screenContentPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(ContactDetailsTokens.cardGroupSpacing),
        modifier = modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            ContactDetailsHeader(header = content.header)
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
    }
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
        verticalArrangement = Arrangement.spacedBy(ContactDetailsTokens.cardSpacing),
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
            start = ContactDetailsTokens.sectionHeaderPadding,
            end = ContactDetailsTokens.sectionHeaderPadding,
            bottom = ContactDetailsTokens.sectionHeaderPadding,
        ),
    )
}

@Composable
private fun screenContentPadding(contentPadding: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = ContactDetailsTokens.screenHorizontalPadding +
            contentPadding.calculateStartPadding(layoutDirection),
        end = ContactDetailsTokens.screenHorizontalPadding +
            contentPadding.calculateEndPadding(layoutDirection),
        top = contentPadding.calculateTopPadding(),
        bottom = ContactDetailsTokens.screenBottomPadding +
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
            phoneticName = null,
            photo = null,
            avatarSeed = "anna-smith",
            isBusiness = false,
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
