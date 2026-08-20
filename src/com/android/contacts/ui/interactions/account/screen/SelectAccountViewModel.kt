package com.android.contacts.ui.interactions.account.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountEffect as Effect
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountUiState as State
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface SelectAccountScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class SelectAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    loadAccounts: LoadAccounts,
) : ViewModel(),
    SelectAccountScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(
        State(titleId = savedStateHandle[KEY_TITLE_RES_ID]),
    )
    override val uiState = _uiState.asStateFlow()

    private val accountFilter: AccountFilter =
        savedStateHandle[KEY_LIST_FILTER] ?: AccountFilter.ALL

    init {
        loadAccounts(accountFilter)
            .onEach {
                _uiState.update { state ->
                    state.copy(accounts = it.map(::AccountUiModel).toImmutableList())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.Dismiss -> {
                emitEffect(Effect.Close(account = null))
            }
            is Action.AccountSelected -> {
                emitEffect(Effect.Close(account = action.account.account))
            }
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    companion object {
        const val KEY_TITLE_RES_ID = "title_res_id"
        const val KEY_LIST_FILTER = "list_filter"
    }
}
