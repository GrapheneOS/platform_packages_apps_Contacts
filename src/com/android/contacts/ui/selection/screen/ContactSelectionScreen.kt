package com.android.contacts.ui.selection.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.components.delayedIndicator
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.core.scrollingScreenPadding
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_DESCRIPTION_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_EMPTY_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_LOADING_TEST_TAG
import com.android.contacts.ui.selection.screen.model.ContactListSection
import com.android.contacts.ui.selection.screen.model.ContactSelectionAction as Action
import com.android.contacts.ui.selection.screen.model.ContactSelectionListState
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState as State
import com.android.contacts.ui.selection.screen.model.ContactUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun ContactSelectionScreen(
    effectHandler: ContactSelectionEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: ContactSelectionScreenModel =
        viewModel<ContactSelectionViewModel>(),
) {
    val state by screenModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    ContactSelectionContent(
        state = state,
        onAction = screenModel::onAction,
        modifier = modifier,
    )

    BackHandler(enabled = state.searchState is SearchState.Searching) {
        screenModel.onAction(Action.CloseClick)
    }
}

@Composable
internal fun ContactSelectionContent(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    ScrollListToTopIfNeeded(
        isLoading = state.listState.isLoading,
        searchQuery = state.listState.searchQuery,
        lazyListState = lazyListState,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            ContactSelectionTopAppBar(
                state = state,
                onAction = onAction,
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        Column(
            Modifier
                .padding(scrollingScreenPadding(contentPadding))
                .consumeWindowInsets(contentPadding)
                .imePadding(),
        ) {
            if (state.description != null) {
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .testTag(CONTACT_SELECTION_DESCRIPTION_TEST_TAG)
                        .padding(16.dp),
                )
            }

            Box(Modifier.fillMaxSize()) {
                if (state.listState.isLoading) {
                    ContactSelectionLoading()
                }
                if (!state.listState.isLoading && state.listState.contacts.isEmpty()) {
                    ContactSelectionEmpty(state.emptyMessageId)
                }
                ContactSelectionList(
                    header = state.listHeader,
                    contactSections = state.listState.contacts,
                    canCreate = state.canCreate,
                    allowMultiple = state.allowMultiple,
                    showAvatars = state.showAvatars,
                    onCreateClick = { onAction(Action.CreateClick) },
                    onContactClick = { onAction(Action.ContactClick(it)) },
                    contentPadding = contentPadding,
                    lazyListState = lazyListState,
                )
            }
        }
    }
}

@Composable
private fun ScrollListToTopIfNeeded(
    isLoading: Boolean,
    searchQuery: String?,
    lazyListState: LazyListState,
) {
    var scrolledQuery by rememberSaveable { mutableStateOf(searchQuery) }
    if (!isLoading && searchQuery != scrolledQuery) {
        lazyListState.requestScrollToItem(0)
        scrolledQuery = searchQuery
    }
}

@Composable
private fun ContactSelectionLoading() {
    LinearProgressIndicator(
        modifier = Modifier
            .testTag(CONTACT_SELECTION_LOADING_TEST_TAG)
            .height(2.dp)
            .fillMaxWidth()
            .delayedIndicator(),
    )
}

@Composable
private fun BoxScope.ContactSelectionEmpty(emptyMessageId: Int?) {
    Text(
        text = stringResource(emptyMessageId ?: return),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .testTag(CONTACT_SELECTION_EMPTY_TEST_TAG)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(32.dp)
            .align(Alignment.Center),
    )
}

@PreviewLightDark
@Composable
private fun ContactSelectionLoadingPreview() {
    ContactsPreviewTheme {
        ContactSelectionContent(
            state = State(
                title = "Choose a contact",
                description = "Choose the contact you want to link with John:",
                emptyMessageId = R.string.noContacts,
                searchState = SearchState.Regular,
                listState = ContactSelectionListState(isLoading = true),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionEmptyPreview() {
    ContactsPreviewTheme {
        ContactSelectionContent(
            state = State(
                title = "Choose a contact",
                emptyMessageId = R.string.noContacts,
                searchState = SearchState.Regular,
                listState = ContactSelectionListState(isLoading = false),
                listHeader = "0 contacts",
                canCreate = true,
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionListPreview() {
    ContactsPreviewTheme {
        ContactSelectionContent(
            state = State(
                title = "Choose Contacts",
                emptyMessageId = R.string.noContacts,
                listState = ContactSelectionListState(
                    contacts = persistentMapOf(
                        ContactListSection.Favorites to persistentListOf(
                            ContactUiModel(
                                key = ContactItemKey.Local(1L),
                                name = "Alice",
                                isSelected = true,
                            ),
                        ),
                        ContactListSection.Alphabetical("B") to persistentListOf(
                            ContactUiModel(key = ContactItemKey.Local(2L), name = "Brian"),
                            ContactUiModel(key = ContactItemKey.Local(3L), name = "Bob"),
                        ),
                        ContactListSection.Alphabetical("C") to persistentListOf(
                            ContactUiModel(
                                key = ContactItemKey.Local(4L),
                                name = "Carl",
                                isSelected = true,
                            ),
                        ),
                    ),
                ),
                searchState = SearchState.Regular,
                allowMultiple = true,
            ),
            onAction = {},
        )
    }
}
