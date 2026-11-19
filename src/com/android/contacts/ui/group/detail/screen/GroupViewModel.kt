package com.android.contacts.ui.group.detail.screen

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract.Contacts
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.R
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.groups.model.GroupSendTargets
import com.android.contacts.domain.groups.usecase.AddGroupMembers
import com.android.contacts.domain.groups.usecase.DeleteGroup
import com.android.contacts.domain.groups.usecase.GetGroup
import com.android.contacts.domain.groups.usecase.GetGroupMembers
import com.android.contacts.domain.groups.usecase.GetGroupSendTargets
import com.android.contacts.domain.groups.usecase.RemoveGroupMembers
import com.android.contacts.group.GroupUtil
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.detail.screen.mapper.GroupUiStateMapper
import com.android.contacts.ui.group.detail.screen.model.GroupAction as Action
import com.android.contacts.ui.group.detail.screen.model.GroupContentState as Content
import com.android.contacts.ui.group.detail.screen.model.GroupEffect as Effect
import com.android.contacts.ui.group.detail.screen.model.GroupState as State
import com.android.contacts.ui.group.detail.screen.model.GroupUiState as UiState
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface GroupScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<UiState>

    fun onAction(action: Action)
}

@HiltViewModel
internal class GroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGroup: GetGroup,
    getGroupMembers: GetGroupMembers,
    private val getGroupSendTargets: GetGroupSendTargets,
    private val addGroupMembers: AddGroupMembers,
    private val removeGroupMembers: RemoveGroupMembers,
    private val deleteGroup: DeleteGroup,
    uiStateMapper: GroupUiStateMapper,
) : ViewModel(),
    GroupScreenModel {

    private val groupId: Long? = parseGroupId(savedStateHandle[GroupActivity.EXTRA_GROUP_URI])

    private val state = savedStateHandle.getMutableStateFlow(KEY_STATE, State())

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val content: StateFlow<Content> =
        when (groupId) {
            null -> {
                emitEffect(Effect.ShowMessage(R.string.groupLoadErrorToast, shouldSurviveClose = true))
                emitEffect(Effect.Close)
                emptyFlow()
            }
            else -> {
                combine(
                    getGroup(groupId),
                    getGroupMembers(groupId),
                ) { details, members ->
                    when (details) {
                        null -> Content.Deleted
                        else -> Content.Loaded(details, members)
                    }
                }
            }
        }
            .distinctUntilChanged()
            .onEach { if (it is Content.Deleted) emitEffect(Effect.Close) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS),
                initialValue = Content.Loading,
            )

    override val uiState: StateFlow<UiState> =
        combine(state, content, uiStateMapper::map)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS),
                initialValue = UiState.Loading,
            )

    @Suppress("CyclomaticComplexMethod")
    override fun onAction(action: Action) {
        when (action) {
            Action.Dismissed -> onDismissed()
            is Action.ContactClicked -> onContactClicked(action.key)
            is Action.ContactLongClicked -> toggleSelection(action.key)
            Action.SelectAllClicked -> selectAll()
            Action.SelectionCleared -> clearSelection()
            Action.CreateContactClicked -> onCreateContactClicked()
            Action.AddMembersClicked -> onAddMembersClicked()
            is Action.MembersPicked -> onMembersPicked(action.contactIds)
            is Action.SendClicked -> onSendClicked(action.target)
            Action.RenameClicked -> onRenameClicked()
            Action.DeleteClicked -> onDeleteClicked()
            Action.DeleteConfirmed -> onDeleteConfirmed()
            Action.DeleteDismissed -> setDeleteConfirmationVisible(false)
            Action.RemoveSelectedClicked -> onRemoveSelectedClicked()
            is Action.SaveCompleted -> onSaveCompleted(action)
        }
    }

    private fun onDismissed() {
        when {
            selectedMemberIds().isNotEmpty() -> clearSelection()
            else -> emitEffect(Effect.Close)
        }
    }

    private fun onContactClicked(key: ContactItemKey) {
        when {
            selectedMemberIds().isNotEmpty() ->
                toggleSelection(key)
            key is ContactItemKey.Local ->
                emitEffect(
                    Effect.OpenContact(ContentUris.withAppendedId(Contacts.CONTENT_URI, key.id)),
                )
        }
    }

    private fun toggleSelection(key: ContactItemKey) {
        state.update { state ->
            val selectedKeys = state.selectedKeys
            state.copy(
                selectedKeys = when (key) {
                    in selectedKeys -> selectedKeys - key
                    else -> selectedKeys + key
                },
            )
        }
    }

    private fun selectAll() {
        val loaded = loaded() ?: return
        state.update { state ->
            state.copy(selectedKeys = loaded.members.mapTo(mutableSetOf()) { it.key })
        }
    }

    private fun clearSelection() {
        state.update { it.copy(selectedKeys = emptySet()) }
    }

    private fun onCreateContactClicked() {
        val loaded = loaded() ?: return
        emitEffect(Effect.CreateContact(loaded.details.group))
    }

    private fun onAddMembersClicked() {
        val loaded = loaded() ?: return
        emitEffect(
            Effect.PickMembers(
                account = loaded.details.group.account,
                existingMemberIds = loaded.memberIds(),
            ),
        )
    }

    private fun onMembersPicked(contactIds: Set<Long>) {
        if (contactIds.isEmpty()) return

        viewModelScope.launch {
            val loaded = waitForLoaded()
            val isUpdated = addGroupMembers(
                group = loaded.details.group,
                contactIds = contactIds,
                callbackActivity = GroupActivity::class.java,
            )
            if (!isUpdated) {
                emitEffect(Effect.ShowMessage(R.string.groupSavedErrorToast))
            }
        }
    }

    private fun onSendClicked(target: SendToGroupTarget) {
        val loaded = loaded() ?: return
        val contactIds = selectedMemberIds().ifEmpty { loaded.memberIds() }
        clearSelection()
        viewModelScope.launch {
            val kind = when (target) {
                SendToGroupTarget.SMS -> ContactDataKind.Phone()
                SendToGroupTarget.EMAIL -> ContactDataKind.Email
            }
            val targets = getGroupSendTargets(contactIds, kind)
            handleGroupSendTargets(target, targets)
        }
    }

    private fun handleGroupSendTargets(
        target: SendToGroupTarget,
        targets: GroupSendTargets,
    ) {
        when (targets) {
            GroupSendTargets.None -> {
                val message = when (target) {
                    SendToGroupTarget.SMS -> R.string.groupContactsWithoutPhonesToast
                    SendToGroupTarget.EMAIL -> R.string.groupContactsWithoutEmailsToast
                }
                emitEffect(Effect.ShowMessage(message))
            }
            is GroupSendTargets.Direct -> {
                if (targets.hasContactsWithoutValue) {
                    val message = when (target) {
                        SendToGroupTarget.SMS -> R.string.groupSomeContactsNoPhonesToast
                        SendToGroupTarget.EMAIL -> R.string.groupSomeContactsNoEmailsToast
                    }
                    emitEffect(Effect.ShowMessage(message))
                }
                emitEffect(Effect.SendTo(target, targets.values))
            }
            is GroupSendTargets.NeedsPicker -> {
                emitEffect(
                    Effect.PickSendTargets(
                        target = target,
                        contactIds = targets.contactIds,
                        defaultSelection = targets.defaultSelection,
                    ),
                )
            }
        }
    }

    private fun onRenameClicked() {
        val loaded = loaded() ?: return
        emitEffect(Effect.EditName(loaded.details.group))
    }

    private fun onDeleteClicked() {
        val loaded = loaded() ?: return
        when {
            loaded.members.isEmpty() -> onDeleteConfirmed()
            else -> setDeleteConfirmationVisible(true)
        }
    }

    private fun onDeleteConfirmed() {
        val loaded = loaded() ?: return
        setDeleteConfirmationVisible(false)
        viewModelScope.launch {
            deleteGroup(loaded.details.group.id)
        }
    }

    private fun setDeleteConfirmationVisible(isVisible: Boolean) {
        state.update { it.copy(isDeleteConfirmationVisible = isVisible) }
    }

    private fun onRemoveSelectedClicked() {
        val loaded = loaded() ?: return
        val contactIds = selectedMemberIds().takeIf { it.isNotEmpty() } ?: return
        clearSelection()
        viewModelScope.launch {
            val isUpdated = removeGroupMembers(
                group = loaded.details.group,
                contactIds = contactIds,
                callbackActivity = GroupActivity::class.java,
            )
            if (!isUpdated) {
                emitEffect(Effect.ShowMessage(R.string.groupSavedErrorToast))
            }
        }
    }

    private fun onSaveCompleted(action: Action.SaveCompleted) {
        val messageId = when {
            !action.isSuccessful -> R.string.groupSavedErrorToast
            else -> when (action.action) {
                GroupUtil.ACTION_UPDATE_GROUP -> R.string.groupUpdatedToast
                GroupUtil.ACTION_ADD_TO_GROUP -> R.string.groupMembersAddedToast
                GroupUtil.ACTION_REMOVE_FROM_GROUP -> R.string.groupMembersRemovedToast
                else -> return
            }
        }
        emitEffect(Effect.ShowMessage(messageId))
    }

    private fun parseGroupId(groupUri: Uri?): Long? {
        if (!GroupUtil.isGroupUri(groupUri)) return null
        return groupUri?.lastPathSegment?.toLongOrNull()?.takeIf { it >= 0 }
    }

    private fun loaded(): Content.Loaded? = content.value as? Content.Loaded

    private suspend fun waitForLoaded(): Content.Loaded {
        return content.filterIsInstance<Content.Loaded>().first()
    }

    private fun Content.Loaded.memberIds(): Set<Long> {
        return members.mapNotNullTo(mutableSetOf()) { (it.key as? ContactItemKey.Local)?.id }
    }

    private fun selectedMemberIds(): Set<Long> {
        val memberIds = loaded()?.memberIds() ?: return emptySet()
        return state.value.selectedKeys
            .mapNotNullTo(mutableSetOf()) { (it as? ContactItemKey.Local)?.id }
            .intersect(memberIds)
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    companion object {
        private const val KEY_STATE = "state"

        @VisibleForTesting
        const val STATE_TIMEOUT_MILLIS = 5_000L
    }
}
