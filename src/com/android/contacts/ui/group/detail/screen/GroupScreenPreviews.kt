package com.android.contacts.ui.group.detail.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.model.ContactListSection
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.common.model.ContactUiModel
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.group.detail.screen.model.GroupUiState as State
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@PreviewLightDark
@Composable
private fun GroupLoadingPreview() {
    ContactsPreviewTheme {
        GroupContent(uiState = State.Loading, onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun GroupEmptyPreview() {
    ContactsPreviewTheme {
        GroupContent(
            uiState = previewContent(
                contacts = persistentMapOf(),
                subtitle = null,
            ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GroupContentPreview() {
    ContactsPreviewTheme {
        GroupContent(uiState = previewContent(), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun GroupSelectionPreview() {
    ContactsPreviewTheme {
        GroupContent(
            uiState = previewContent(selectedIds = setOf(2L)).copy(
                title = "1 selected",
                subtitle = null,
                isSelectionMode = true,
                canAddMembers = false,
                canRemoveMembers = true,
                canEditGroup = false,
            ),
            onAction = {},
        )
    }
}

private fun previewContent(
    selectedIds: Set<Long> = emptySet(),
    contacts: ContactSectionedList = persistentMapOf(
        ContactListSection.Alphabetical("A") to persistentListOf(
            previewContact(id = 1L, name = "Alice", selectedIds = selectedIds),
            previewContact(id = 2L, name = "Anna", selectedIds = selectedIds),
        ),
        ContactListSection.Alphabetical("B") to persistentListOf(
            previewContact(id = 3L, name = "Bob", selectedIds = selectedIds),
        ),
    ),
    subtitle: String? = "3 contacts",
) = State.Content(
    title = "Family",
    subtitle = subtitle,
    contacts = contacts,
    isSelectionMode = false,
    canAddMembers = true,
    canRemoveMembers = false,
    canEditGroup = true,
    canSend = contacts.isNotEmpty(),
    canSelectAll = contacts.isNotEmpty(),
    deleteConfirmationMessage = null,
)

private fun previewContact(
    id: Long,
    name: String,
    selectedIds: Set<Long>,
) = ContactUiModel(
    key = ContactItemKey.Local(id),
    name = name,
    isSelected = id in selectedIds,
)
