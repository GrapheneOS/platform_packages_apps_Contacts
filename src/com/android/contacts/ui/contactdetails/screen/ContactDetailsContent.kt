package com.android.contacts.ui.contactdetails.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.contacts.R
import com.android.contacts.ui.contactdetails.common.ContactDetailsCallingSimDialog
import com.android.contacts.ui.contactdetails.common.ContactDetailsProgressDialog
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.common.ContactDetailsTopAppBar
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State

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
    var isNameHidden by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ContactDetailsTopAppBar(
                title = loaded?.header?.displayName.orEmpty(),
                isTitleVisible = loaded != null && isNameHidden,
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
                    isNameHidden = isNameHidden,
                    onNameHiddenChanged = { hidden -> isNameHidden = hidden },
                )
            }
        }
    }

    val linkProgress = uiState.linkProgress
    if (linkProgress != null) {
        ContactDetailsProgressDialog(operation = linkProgress)
    }

    val callingSim = loaded?.callingSim
    if (callingSim != null && uiState.isCallingSimPickerVisible) {
        ContactDetailsCallingSimDialog(
            callingSim = callingSim,
            onConfirm = { selections -> onAction(Action.CallingSimPicked(selections)) },
            onDismiss = { onAction(Action.CallingSimDismissed) },
        )
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
