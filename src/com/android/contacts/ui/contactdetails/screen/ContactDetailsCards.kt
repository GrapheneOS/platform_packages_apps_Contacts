package com.android.contacts.ui.contactdetails.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.android.contacts.R
import com.android.contacts.ui.contactdetails.common.ContactDetailsHeader
import com.android.contacts.ui.contactdetails.common.ContactDetailsOverlayHeight
import com.android.contacts.ui.contactdetails.common.ContactDetailsQuickActions
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.common.measuredInto
import com.android.contacts.ui.contactdetails.common.rememberOverlayHeight
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import kotlinx.collections.immutable.ImmutableList

private const val VISIBLE_ALPHA = 1f
private const val HIDDEN_ALPHA = 0f

@Composable
internal fun ContactDetailsCards(
    content: Content.Loaded,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    isNameHidden: Boolean,
    onNameHiddenChanged: (Boolean) -> Unit,
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
                headerHeight = headerHeight.pixels,
                quickActionsHeight = quickActionsHeight.pixels,
            )
        }

        ContactDetailsHeaderOverlay(
            header = content.header,
            onAction = onAction,
            contentPadding = contentPadding,
            listState = listState,
            height = headerHeight,
            alpha = overlayAlpha,
            isNameHidden = isNameHidden,
            onNameHiddenChanged = onNameHiddenChanged,
        )

        ContactDetailsQuickActionsOverlay(
            quickActions = content.quickActions,
            onAction = onAction,
            contentPadding = contentPadding,
            listState = listState,
            height = quickActionsHeight,
            alpha = overlayAlpha,
        )
    }
}

@Composable
private fun ContactDetailsHeaderOverlay(
    header: ContactHeaderUiModel,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    height: ContactDetailsOverlayHeight,
    alpha: Float,
    isNameHidden: Boolean,
    onNameHiddenChanged: (Boolean) -> Unit,
) {
    val contentTop = with(LocalDensity.current) {
        contentPadding.calculateTopPadding().toPx()
    }
    val nameLabel = stringResource(R.string.nameLabelsGroup)

    ContactDetailsHeader(
        header = header,
        onNameLongClick = { onAction(Action.CopyClick(nameLabel, header.displayName)) },
        onNameBottomChanged = { bottom ->
            val hidden = bottom <= contentTop

            if (hidden != isNameHidden) {
                onNameHiddenChanged(hidden)
            }
        },
        modifier = Modifier
            .alpha(alpha)
            .offset {
                IntOffset(
                    x = 0,
                    y = listState.headerOffset(height.pixels),
                )
            }
            .measuredInto(height)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontalContentPadding(contentPadding)),
    )
}

@Composable
private fun ContactDetailsQuickActionsOverlay(
    quickActions: ImmutableList<ContactQuickActionUiModel>,
    onAction: (Action) -> Unit,
    contentPadding: PaddingValues,
    listState: LazyListState,
    height: ContactDetailsOverlayHeight,
    alpha: Float,
) {
    ContactDetailsQuickActions(
        quickActions = quickActions,
        onActionClick = { action -> onAction(Action.EntryClick(action)) },
        modifier = Modifier
            .alpha(alpha)
            .offset {
                IntOffset(
                    x = 0,
                    y = listState.quickActionsOffset(),
                )
            }
            .measuredInto(height)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontalContentPadding(contentPadding))
            .padding(bottom = Tokens.quickActionPinnedPadding),
    )
}
