package com.android.contacts.ui.group.detail.screen.mapper

import android.content.res.Resources
import com.android.contacts.R
import com.android.contacts.ui.common.mapper.ContactSectionedListMapper
import com.android.contacts.ui.common.util.getMessageFormat
import com.android.contacts.ui.group.detail.screen.model.GroupContentState
import com.android.contacts.ui.group.detail.screen.model.GroupState
import com.android.contacts.ui.group.detail.screen.model.GroupUiState
import javax.inject.Inject

internal fun interface GroupUiStateMapper {
    fun map(
        state: GroupState,
        content: GroupContentState,
    ): GroupUiState
}

internal class GroupUiStateMapperImpl @Inject constructor(
    private val resources: Resources,
    private val contactSectionedListMapper: ContactSectionedListMapper,
) : GroupUiStateMapper {

    override fun map(
        state: GroupState,
        content: GroupContentState,
    ): GroupUiState {
        return when (content) {
            GroupContentState.Loading,
            GroupContentState.Deleted,
            -> GroupUiState.Loading
            is GroupContentState.Loaded -> mapLoaded(content, state)
        }
    }

    private fun mapLoaded(
        content: GroupContentState.Loaded,
        state: GroupState,
    ): GroupUiState.Content {
        val group = content.details.group
        val members = content.members
        val selectedKeys = state.selectedKeys intersect members.mapTo(mutableSetOf()) { it.key }
        val isSelectionMode = selectedKeys.isNotEmpty()

        return GroupUiState.Content(
            title = when {
                isSelectionMode -> resources.getQuantityString(
                    R.plurals.contactPickerSelectedTitle,
                    selectedKeys.size,
                    selectedKeys.size,
                )
                else -> group.name
            },
            subtitle = when {
                isSelectionMode || members.isEmpty() -> null
                else -> resources.getMessageFormat(
                    R.string.contacts_count,
                    mapOf("count" to members.size),
                )
            },
            contacts = contactSectionedListMapper.map(
                contacts = members,
                selectedKeys = selectedKeys,
            ),
            isSelectionMode = isSelectionMode,
            canAddMembers = content.details.isMembershipEditable && !isSelectionMode,
            canRemoveMembers = content.details.isMembershipEditable && isSelectionMode,
            canEditGroup = !group.isReadOnly && !isSelectionMode,
            canSend = members.isNotEmpty(),
            canSelectAll = selectedKeys.size < members.size,
            deleteConfirmationMessage = when {
                state.isDeleteConfirmationVisible ->
                    resources.getString(R.string.delete_group_dialog_message, group.name)
                else -> null
            },
        )
    }
}
