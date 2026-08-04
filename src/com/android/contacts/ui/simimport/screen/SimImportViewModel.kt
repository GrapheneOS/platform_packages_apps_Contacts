package com.android.contacts.ui.simimport.screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimCards
import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.model.SimCard
import com.android.contacts.model.SimContact
import com.android.contacts.ui.UIIntents.EXTRA_SUBSCRIPTION_ID
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapper
import com.android.contacts.ui.simimport.screen.model.AccountContactsEntry
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import com.android.contacts.ui.simimport.screen.model.SimImportEffect as Effect
import com.android.contacts.ui.simimport.screen.model.SimImportUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update

internal interface SimImportScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class SimImportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    loadSimCards: LoadSimCards,
    private val getDefaultAccount: GetDefaultAccount,
    loadSimContacts: LoadSimContacts,
    loadAccounts: LoadAccounts,
    private val simContactUiModelMapper: SimContactUiModelMapper,
    private val startSimImport: StartSimImport,
) : ViewModel(),
    SimImportScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(State())
    override val uiState = _uiState.asStateFlow()

    private val simContacts =
        MutableStateFlow<ImmutableList<SimContactUiModel>?>(null)
    private val existingContacts =
        MutableStateFlow<ImmutableMap<AccountModel, ImmutableSet<Int>>>(persistentMapOf())
    private val selectedContacts = MutableStateFlow(restoreSelectedContacts())

    private val subscriptionId: Int = savedStateHandle[EXTRA_SUBSCRIPTION_ID] ?: run {
        Log.w(TAG, "$EXTRA_SUBSCRIPTION_ID missing, defaulting to ${SimCard.NO_SUBSCRIPTION_ID}")
        SimCard.NO_SUBSCRIPTION_ID
    }

    init {
        if (subscriptionId != SimCard.NO_SUBSCRIPTION_ID) {
            loadSimCards()
                .filter { simCards -> simCards.none { it.subscriptionId == subscriptionId } }
                .take(1)
                .onEach {
                    Log.i(TAG, "SIM card removed, aborting SIM import.")
                    emitEffect(Effect.Close(isSuccessful = false))
                }
                .launchIn(viewModelScope)
        }

        loadSimContacts(subscriptionId)
            .onEach { result ->
                simContacts.value =
                    result.contacts.map(simContactUiModelMapper::map).toImmutableList()
                existingContacts.value = result.existingContactsInAccounts
                    .mapValues { (_, set) ->
                        set.map { it.recordNumber }.distinct().toImmutableSet()
                    }
                    .toImmutableMap()
            }
            .launchIn(viewModelScope)

        loadAccounts()
            .onEach { accounts ->
                val uiAccounts = accounts.map(::AccountUiModel).toImmutableList()
                _uiState.update { state ->
                    state.copy(
                        accounts = uiAccounts,
                        currentAccount = findCurrentAccount(uiAccounts),
                    )
                }
            }
            .launchIn(viewModelScope)

        // Build map of selected contacts per account
        combine(
            uiState.mapNotNull { it.accounts }.distinctUntilChanged(),
            simContacts.filterNotNull(),
        ) { accounts, contacts ->
            selectedContacts.update { oldSelectedContactsMap ->
                accounts.associate { account ->
                    val oldSelectedContacts = oldSelectedContactsMap[account.account]
                    val selectedContacts = if (oldSelectedContacts == null) {
                        // If we get a new account, select all contacts for import
                        contacts.map { it.recordNumber }
                    } else {
                        // Otherwise let's keep the previously selected contacts selected
                        val newContactsIds = contacts.map { it.recordNumber }
                        oldSelectedContacts.filter { newContactsIds.contains(it) }
                    }
                    account.account to selectedContacts.toImmutableSet()
                }.toImmutableMap()
            }
        }
            .launchIn(viewModelScope)

        // Set SIM contacts to show in the UI according to the current account,
        // and based on whether they are selected or already imported
        combine(
            uiState.map { it.currentAccount }.distinctUntilChanged(),
            simContacts.filterNotNull(),
            selectedContacts,
            existingContacts,
        ) { account, simContacts, selectedContacts, existingContacts ->
            val account = account ?: return@combine
            _uiState.update { state ->
                val (contactsAlreadyImported, contactsToImport) = simContacts.partition { contact ->
                    existingContacts.contains(account, contact)
                }
                state.copy(
                    contactsToImport = contactsToImport.map {
                        SelectableItem(
                            item = it,
                            isSelected = selectedContacts.contains(account, it),
                        )
                    }.toImmutableList(),
                    contactsAlreadyImported = contactsAlreadyImported.toImmutableList(),
                )
            }
        }
            .launchIn(viewModelScope)

        uiState
            .mapNotNull { it.currentAccount }
            .distinctUntilChanged()
            .onEach {
                savedStateHandle[KEY_CURRENT_ACCOUNT] = it.account
            }.launchIn(viewModelScope)

        selectedContacts
            .onEach {
                savedStateHandle[KEY_SELECTED_CONTACTS] = it.map { (account, contacts) ->
                    AccountContactsEntry(account = account, contactNumbers = contacts)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CloseClicked -> close(isSuccessful = false)
            is Action.AccountChanged -> changeAccount(action.account)
            is Action.ContactSelectionChanged ->
                changeContactSelection(action.contact, action.isSelected)
            Action.SelectAllClicked -> selectAll()
            Action.DeselectAllClicked -> deselectAll()
            Action.ImportClicked -> startImport()
        }
    }

    private fun restoreSelectedContacts(): ImmutableMap<AccountModel, ImmutableSet<Int>> {
        val entries = savedStateHandle.get<List<AccountContactsEntry>>(KEY_SELECTED_CONTACTS)
        return entries
            ?.associate { it.account to it.contactNumbers.toImmutableSet() }
            ?.toImmutableMap()
            ?: persistentMapOf()
    }

    private fun findCurrentAccount(accounts: List<AccountUiModel>): AccountUiModel? {
        val currentAccount = uiState.value.currentAccount
        return currentAccount
            ?.takeIf { accounts.contains(it) }
            ?: getSavedAccount(accounts)
            ?: getDefaultAccountFromList(accounts)
            ?: accounts.firstOrNull()
    }

    private fun getSavedAccount(accounts: List<AccountUiModel>): AccountUiModel? {
        return savedStateHandle.get<AccountModel>(KEY_CURRENT_ACCOUNT)?.let { savedAccount ->
            accounts.firstOrNull { it.account == savedAccount }
        }
    }

    private fun getDefaultAccountFromList(
        accounts: List<AccountUiModel>,
    ): AccountUiModel? {
        return getDefaultAccount()?.let { defaultAccount ->
            accounts.firstOrNull { it.account == defaultAccount }
        }
    }

    private fun close(isSuccessful: Boolean) {
        emitEffect(Effect.Close(isSuccessful))
    }

    private fun changeAccount(account: AccountUiModel) {
        _uiState.update { state -> state.copy(currentAccount = account) }
    }

    private fun changeContactSelection(contact: SimContactUiModel, isSelected: Boolean) {
        val account = uiState.value.currentAccount ?: return
        selectedContacts.update { map ->
            val currentSelectedContacts = map[account.account].orEmpty().toPersistentSet()
            map.toPersistentMap() + (
                account.account to when {
                    isSelected -> currentSelectedContacts + contact.recordNumber
                    else -> currentSelectedContacts - contact.recordNumber
                }
                )
        }
    }

    private fun selectAll() {
        val account = uiState.value.currentAccount ?: return
        val allContacts = uiState.value.contactsToImport ?: return
        selectedContacts.update { map ->
            map.toPersistentMap() +
                (account.account to allContacts.map { it.item.recordNumber }.toImmutableSet())
        }
    }

    private fun deselectAll() {
        val account = uiState.value.currentAccount ?: return
        selectedContacts.update { map ->
            map.toPersistentMap() + (account.account to persistentSetOf())
        }
    }

    private fun startImport() {
        val state = uiState.value
        val account = state.currentAccount?.account ?: return
        val selectedContacts = getSelectedContacts() ?: return

        startSimImport(subscriptionId, selectedContacts, account)
        close(isSuccessful = true)
    }

    private fun getSelectedContacts(): ImmutableList<SimContact>? {
        return uiState.value.contactsToImport
            ?.filter { it.isSelected }
            ?.map { simContactUiModelMapper.unmap(it.item) }
            ?.ifEmpty { null }
            ?.toImmutableList()
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private fun Map<AccountModel, Set<Int>>.contains(
        account: AccountUiModel,
        contact: SimContactUiModel,
    ) = this[account.account]?.contains(contact.recordNumber) == true

    private companion object {
        const val TAG = "SimImportViewModel"
        const val KEY_CURRENT_ACCOUNT = "accountHeaderSelectedAccount"
        const val KEY_SELECTED_CONTACTS = "selectedContacts"
    }
}
