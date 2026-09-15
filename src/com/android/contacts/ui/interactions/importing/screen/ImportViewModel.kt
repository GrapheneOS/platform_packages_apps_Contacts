package com.android.contacts.ui.interactions.importing.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect
import com.android.contacts.ui.interactions.importing.screen.model.ImportUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

internal interface ImportScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ImportViewModel @Inject constructor(
    private val settingsAvailabilityRepository: SettingsAvailabilityRepository,
    loadSimCards: LoadSimCards,
    simCardOptionMapper: SimCardOptionMapper,
    loadAccounts: LoadAccounts,
) : ViewModel(),
    ImportScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private var accounts: List<AccountDisplayModel>? = null

    override val uiState =
        combine(
            settingsAvailabilityRepository::getSettingsAvailability.asFlow(),
            loadSimCards(),
        ) { settingsAvailability, simCards ->
            State(
                isVCardImportAvailable = settingsAvailability.isImportFromVCardAvailable,
                simCardOptions = simCards
                    .map(simCardOptionMapper::map)
                    .toImmutableList(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_STOP_TIMEOUT_MILLIS),
            initialValue = State(),
        )

    init {
        loadAccounts()
            .onEach { accounts = it }
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
        // Accounts should be loaded by now
        val accounts = accounts ?: return

        if (accounts.size > 1) {
            emitEffect(Effect.OpenSelectAccount)
        } else {
            openVCardImport(accounts.firstOrNull()?.account)
        }
    }

    private fun openVCardImport(account: AccountModel?) {
        emitEffect(Effect.OpenVCardImport(account))
    }

    private companion object {
        const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
