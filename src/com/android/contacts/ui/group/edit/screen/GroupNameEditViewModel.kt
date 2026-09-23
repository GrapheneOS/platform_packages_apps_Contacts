package com.android.contacts.ui.group.edit.screen

import android.app.Activity
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.usecase.CreateOrEditGroupName
import com.android.contacts.domain.groups.usecase.GetGroupNameMaxLenght
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditInputError as InputError
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal interface GroupNameEditScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class GroupNameEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getGroups: GetGroups,
    getGroupNameMaxLenght: GetGroupNameMaxLenght,
    private val createOrEditGroupName: CreateOrEditGroupName,
) : ViewModel(),
    GroupNameEditScreenModel {

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<State>(State.Loading)
    override val uiState = _uiState.asStateFlow()

    private var currentName: String?
        get() = savedStateHandle[KEY_CURRENT_NAME]
        set(value) {
            savedStateHandle[KEY_CURRENT_NAME] = value
        }

    private val groupId: Long? =
        savedStateHandle.get<Long>(GroupNameEditActivity.EXTRA_GROUP_ID)
    private val groupName: String? =
        savedStateHandle.get<String>(GroupNameEditActivity.EXTRA_GROUP_NAME)
    private val account: AccountModel? =
        savedStateHandle.get<AccountModel>(GroupNameEditActivity.EXTRA_ACCOUNT)
    private val callbackActivity: Class<out Activity>? =
        savedStateHandle.get<Class<out Activity>>(GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY)
    private val callbackAction =
        savedStateHandle.get<String>(GroupNameEditActivity.EXTRA_CALLBACK_ACTION)

    private var existingGroupNames = emptyList<String>()

    init {
        if (callbackActivity == null) {
            Log.e(TAG, "Calling activity class missing")
            emitEffect(Effect.Close(isSuccessful = false))
        }

        getGroups(account)
            .onEach { groups ->
                if (groups == null) return@onEach
                existingGroupNames = groups.map { it.name }

                _uiState.update { state ->
                    if (state is State.Ready) return@update state

                    State.Ready(
                        isEdit = groupId != null,
                        name = currentName ?: groupName ?: "",
                        maxLenght = getGroupNameMaxLenght(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

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
            if (state !is State.Ready) return@update state
            currentName = value
            state.copy(name = value, inputError = null)
        }
    }

    private fun onOkClick() {
        val state = uiState.value as? State.Ready ?: return
        val name = state.name.trim()

        when {
            state.isEdit && name == groupName -> {
                emitEffect(Effect.Close(isSuccessful = true))
            }
            existingGroupNames.contains(name) -> {
                _uiState.value = state.copy(inputError = InputError.DUPLICATED_NAME)
            }
            else -> {
                createOrEdit(name)
            }
        }
    }

    private fun createOrEdit(name: String) {
        createOrEditGroupName(
            account = account,
            groupId = groupId,
            groupName = name,
            callbackActivity = callbackActivity ?: return,
            callbackAction = callbackAction,
        )
        emitEffect(Effect.Close(isSuccessful = true))
    }

    private companion object {
        const val TAG = "GroupNameEditActivity"

        @VisibleForTesting
        const val KEY_CURRENT_NAME = "current_name"
    }
}
