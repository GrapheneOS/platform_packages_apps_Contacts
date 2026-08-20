package com.android.contacts.ui.interactions.account.screen

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountEffect as Effect
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountUiState as State
import com.android.contacts.ui.simimport.screen.mapper.AccountUiModelMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal interface SelectAccountScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class SelectAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    loadAccounts: LoadAccounts,
    accountUiModelMapper: AccountUiModelMapper,
) : ViewModel(),
    SelectAccountScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val accountFilter: AccountFilter =
        savedStateHandle[KEY_LIST_FILTER] ?: AccountFilter.ALL

    @StringRes
    private val titleResId: Int? = savedStateHandle[KEY_TITLE_RES_ID]

    override val uiState =
        loadAccounts(accountFilter)
            .map {
                State(
                    titleId = titleResId,
                    accounts = it.map(accountUiModelMapper::map).toImmutableList(),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATEFLOW_STOP_TIMEOUT_MILLIS),
                initialValue = State(titleId = titleResId),
            )

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
        private const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
