@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.selection.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_BACK_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_TITLE_TEST_TAG
import com.android.contacts.ui.selection.screen.model.ContactSelectionAction as Action
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState as State
import kotlinx.coroutines.flow.drop

private const val ENTER_MILLIS = 220
private const val EXIT_FADE_MILLIS = 80

private val SearchFieldHeight = 48.dp

@Composable
internal fun ContactSelectionTopAppBar(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSearching = state.searchState is ContactSelectionSearchState.Searching

    TopAppBar(
        navigationIcon = {
            // While searching, the back button lives inside the search field instead
            AnimatedVisibility(
                visible = !isSearching,
                enter = fadeIn(tween(ENTER_MILLIS)) + expandHorizontally(tween(ENTER_MILLIS)),
                exit = fadeOut(tween(EXIT_FADE_MILLIS)) + shrinkHorizontally(tween(ENTER_MILLIS)),
            ) {
                CloseButton(onClick = { onAction(Action.CloseClick) })
            }
        },
        title = {
            TitleOrSearchField(
                title = state.title,
                searchState = state.searchState,
                onQueryChange = { onAction(Action.SearchQueryChange(it)) },
                onClose = { onAction(Action.CloseClick) },
            )
        },
        actions = {
            AnimatedVisibility(
                visible = state.searchState is ContactSelectionSearchState.Regular,
                enter = fadeIn(tween(ENTER_MILLIS)),
                exit = fadeOut(tween(EXIT_FADE_MILLIS)),
            ) {
                SearchButton(onClick = { onAction(Action.SearchClick) })
            }
            if (state.allowMultiple) {
                SubmitButton(
                    enabled = state.selectedCount > 0,
                    onSubmit = { onAction(Action.SubmitClick) },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier,
    )
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.cancel_button_content_description),
        )
    }
}

@Composable
private fun SearchBackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(CONTACT_SELECTION_SEARCH_BACK_BUTTON_TEST_TAG),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = stringResource(R.string.cancel_button_content_description),
        )
    }
}

@Composable
private fun TitleOrSearchField(
    title: String,
    searchState: ContactSelectionSearchState,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    AnimatedContent(
        targetState = searchState is ContactSelectionSearchState.Searching,
        transitionSpec = {
            fadeIn(tween(ENTER_MILLIS, delayMillis = EXIT_FADE_MILLIS)) togetherWith
                fadeOut(tween(EXIT_FADE_MILLIS))
        },
        contentAlignment = Alignment.CenterStart,
        label = "ContactSelectionTopAppBarTitle",
        modifier = Modifier.padding(end = 4.dp)
    ) { searching ->
        when {
            searching ->
                SearchField(
                    initialQuery = (searchState as? ContactSelectionSearchState.Searching)
                        ?.query
                        .orEmpty(),
                    onQueryChange = onQueryChange,
                    onClose = onClose,
                )
            else ->
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(CONTACT_SELECTION_TITLE_TEST_TAG),
                )
        }
    }
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.searchHint),
        )
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    Button(
        onClick = onSubmit,
        enabled = enabled,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = Modifier
            .testTag(CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG)
            .padding(end = 8.dp),
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.contactPickerDoneButton))
        }
    }
}

@Composable
private fun SearchField(
    initialQuery: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldState = rememberTextFieldState(initialText = initialQuery)
    val focusRequester = remember { FocusRequester() }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )

    BasicTextField(
        state = textFieldState,
        textStyle = textStyle,
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorator = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                SearchBackButton(onClick = onClose)
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f),
                ) {
                    if (textFieldState.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.searchHint),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
                when {
                    textFieldState.text.isNotEmpty() ->
                        SearchClearButton(onClear = { textFieldState.clearText() })
                    else ->
                        Spacer(Modifier.width(16.dp))
                }
            }
        },
        modifier = modifier
            .testTag(CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG)
            .fillMaxWidth()
            .padding(end = 8.dp)
            .heightIn(min = SearchFieldHeight)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .focusRequester(focusRequester),
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .drop(1)
            .collect { onQueryChange(it) }
    }
}

@Composable
private fun SearchClearButton(onClear: () -> Unit) {
    IconButton(
        onClick = onClear,
        modifier = Modifier.testTag(CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(R.string.description_clear_search),
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionTopAppBarRegularPreview() {
    ContactsPreviewColumn {
        ContactSelectionTopAppBar(
            state = State(
                title = "Choose a contact",
                searchState = ContactSelectionSearchState.Regular,
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionTopAppBarMultiplePreview() {
    ContactsPreviewColumn {
        ContactSelectionTopAppBar(
            state = State(
                title = "Choose Contacts",
                searchState = ContactSelectionSearchState.Regular,
                allowMultiple = true,
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionTopAppBarSearchingEmptyPreview() {
    ContactsPreviewColumn {
        ContactSelectionTopAppBar(
            state = State(
                title = "Choose a contact",
                searchState = ContactSelectionSearchState.Searching(""),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionTopAppBarSearchingPreview() {
    ContactsPreviewColumn {
        ContactSelectionTopAppBar(
            state = State(
                title = "Choose a contact",
                searchState = ContactSelectionSearchState.Searching("Alice"),
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactSelectionTopAppBarSearchingAndMultiplePreview() {
    ContactsPreviewColumn {
        ContactSelectionTopAppBar(
            state = State(
                title = "Choose a contact",
                searchState = ContactSelectionSearchState.Searching("Alice"),
                allowMultiple = true,
                selectedCount = 2,
            ),
            onAction = {},
        )
    }
}
