package com.android.contacts.ui.group.edit.screen

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExists
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface GroupNameEditScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class GroupNameEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val checkIfGroupNameExists: CheckIfGroupNameExists,
) : ViewModel(),
    GroupNameEditScreenModel {

    private var currentName: String?
        get() = savedStateHandle[KEY_CURRENT_NAME]
        set(value) {
            savedStateHandle[KEY_CURRENT_NAME] = value
        }

    private val groupId: Long? =
        savedStateHandle.get<Long>(GroupNameEditActivity.EXTRA_GROUP_ID)
    private val groupName: String? =
        savedStateHandle.get<String>(GroupNameEditActivity.EXTRA_GROUP_NAME)
    private val account: AccountModel =
        savedStateHandle.get<AccountModel>(GroupNameEditActivity.EXTRA_ACCOUNT) ?: AccountModel.NULL

    private val _uiState = MutableStateFlow(
        State(
            isEdit = groupId != null,
            name = currentName ?: groupName ?: "",
        ),
    )
    override val uiState = _uiState.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    override fun onAction(action: Action) {
        when (action) {
            Action.Dismissed -> emitEffect(Effect.Close(isSuccessful = false))
            is Action.NameChanged -> onNameChange(action.value)
            Action.OkClicked -> onOkClick()
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun onNameChange(value: String) {
        _uiState.update { state ->
            currentName = value
            state.copy(name = value, isDuplicatedName = false)
        }
    }

    private fun onOkClick() {
        val state = uiState.value
        val name = state.name.trim()

        viewModelScope.launch {
            when {
                state.isEdit && name == groupName -> {
                    emitEffect(Effect.Close(isSuccessful = true))
                }
                checkIfGroupNameExists(account, name) -> {
                    _uiState.value = state.copy(isDuplicatedName = true)
                }
                else -> {
                    createOrEdit(name)
                }
            }
        }
    }

    private fun createOrEdit(name: String) {
        emitEffect(
            when {
                groupId != null ->
                    Effect.EditGroup(
                        groupId = groupId,
                        groupName = name,
                    )
                else ->
                    Effect.CreateGroup(
                        account = account,
                        groupName = name,
                    )
            },
        )
    }

    private companion object {
        @VisibleForTesting
        const val KEY_CURRENT_NAME = "current_name"
    }
}
