package com.android.contacts.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.model.ContactListSection
import com.android.contacts.ui.common.model.ContactSectionedList

private val ITEMS_HORIZONTAL_PADDING = 16.dp
private val ITEMS_TOP_PADDING = 4.dp
private val ITEMS_BOTTOM_PADDING = 32.dp

@Composable
internal fun ContactSectionedListColumn(
    contactSections: ContactSectionedList,
    isSelectable: Boolean,
    showAvatars: Boolean,
    onContactClick: (ContactItemKey) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onContactLongClick: ((ContactItemKey) -> Unit)? = null,
    sectionModifier: (ContactListSection) -> Modifier = { Modifier },
    contactModifier: (ContactListSection, ContactItemKey) -> Modifier = { _, _ -> Modifier },
    leadingContent: LazyListScope.() -> Unit = {},
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(
            start = ITEMS_HORIZONTAL_PADDING,
            end = ITEMS_HORIZONTAL_PADDING,
            top = ITEMS_TOP_PADDING,
            bottom = ITEMS_BOTTOM_PADDING + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        leadingContent()

        contactSections.forEach { (section, contacts) ->
            if (section !is ContactListSection.Undefined) {
                stickyHeader(sectionKey(section)) {
                    ContactListSectionHeader(
                        section = section,
                        modifier = sectionModifier(section),
                    )
                }
            }
            itemsIndexed(
                items = contacts,
                key = { _, contact -> sectionKey(section) + "_" + contact.key },
            ) { index, contact ->
                ContactCell(
                    contact = contact,
                    isSelectable = isSelectable,
                    showAvatar = showAvatars,
                    onClick = { onContactClick(contact.key) },
                    onLongClick = onContactLongClick?.let { { it(contact.key) } },
                    isFirst = index == 0,
                    isLast = index == contacts.lastIndex,
                    modifier = contactModifier(section, contact.key),
                )
            }
        }
    }
}

private fun sectionKey(section: ContactListSection): String {
    return when (section) {
        is ContactListSection.Alphabetical -> "letter_${section.letter}"
        ContactListSection.Favorites -> "favorites"
        ContactListSection.Undefined -> "undefined"
        ContactListSection.Suggestions -> "suggestions"
        is ContactListSection.Directory -> "directory_${section.directoryId}"
    }
}
