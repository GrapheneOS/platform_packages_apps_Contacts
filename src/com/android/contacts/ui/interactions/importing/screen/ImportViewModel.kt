package com.android.contacts.ui.interactions.importing.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.domain.vcard.usecase.CanImportFromVCard
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect
import com.android.contacts.ui.interactions.importing.screen.model.ImportUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface ImportScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ImportViewModel @Inject constructor(
    canImportFromVCard: CanImportFromVCard,
    loadSimCards: LoadSimCards,
    simCardOptionMapper: SimCardOptionMapper,
    loadAccounts: LoadAccounts,
) : ViewModel(),
    ImportScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(
        State(isVCardImportAvailable = canImportFromVCard()),
    )
    override val uiState = _uiState.asStateFlow()

    private val accounts = MutableStateFlow<List<AccountDisplayModel>?>(null)

    init {
        loadSimCards()
            .onEach {
                _uiState.update { state ->
                    state.copy(
                        simCardOptions = it.map(simCardOptionMapper::map).toImmutableList(),
                    )
                }
            }
            .launchIn(viewModelScope)

        loadAccounts(AccountFilter.CONTACTS_INSERTABLE)
            .onEach { accounts.value = it }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.Dismiss -> {
                emitEffect(Effect.Close)
            }
            Action.VCardClick -> {
                onVCardClick()
            }
            is Action.SimOptionClick -> {
                emitEffect(Effect.OpenSimImport(action.simCardOption.subscriptionId))
            }
            is Action.AccountChosen -> {
                openVCardImport(action.account)
            }
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private fun onVCardClick() {
        viewModelScope.launch {
            val accounts = accounts.filterNotNull().first()

            if (accounts.size > 1) {
                emitEffect(Effect.OpenSelectAccount)
            } else {
                openVCardImport(accounts.firstOrNull()?.account)
            }
        }
    }

    private fun openVCardImport(account: AccountModel?) {
        emitEffect(Effect.OpenVCardImport(account))
    }
}
