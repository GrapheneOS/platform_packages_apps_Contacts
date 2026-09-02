package com.android.contacts.ui.contactdetails.screen

import androidx.compose.foundation.lazy.LazyListState

internal const val HEADER_KEY = "header"
internal const val QUICK_ACTIONS_KEY = "quick_actions"

internal fun LazyListState.headerOffset(headerHeight: Int): Int {
    return itemOffset(
        key = HEADER_KEY,
        index = 0,
        scrolledPastOffset = -headerHeight,
    )
}

internal fun LazyListState.quickActionsOffset(): Int {
    return itemOffset(
        key = QUICK_ACTIONS_KEY,
        index = 1,
        scrolledPastOffset = 0,
    ).coerceAtLeast(0)
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
