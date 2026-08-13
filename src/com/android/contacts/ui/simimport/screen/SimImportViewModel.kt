package com.android.contacts.ui.simimport.screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountDisplayModel
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
import com.android.contacts.ui.simimport.screen.mapper.AccountUiModelMapper
import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapper
import com.android.contacts.ui.simimport.screen.model.AccountContactsEntry
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import com.android.contacts.ui.simimport.screen.model.SimImportEffect as Effect
import com.android.contacts.ui.simimport.screen.model.SimImportUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    private val accountUiModelMapper: AccountUiModelMapper,
    private val simContactUiModelMapper: SimContactUiModelMapper,
    private val startSimImport: StartSimImport,
) : ViewModel(),
    SimImportScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val accounts = MutableStateFlow<List<AccountDisplayModel>?>(null)
    private val currentAccount = MutableStateFlow<AccountDisplayModel?>(null)
    private val simContacts = MutableStateFlow<List<SimContact>?>(null)
    private val existingContacts = MutableStateFlow<Map<AccountModel, Set<Int>>>(mapOf())
    private val selectedContacts = MutableStateFlow(restoreSelectedContacts())

    override val uiState: StateFlow<State> =
        combine(
            accounts.filterNotNull(),
            currentAccount,
            simContacts.filterNotNull(),
            existingContacts,
            selectedContacts,
            ::buildState,
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_STOP_TIMEOUT_MILLIS),
            initialValue = State.Loading,
        )

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
                simContacts.value = result.contacts
                existingContacts.value = result.existingContactsInAccounts
                    .mapValues { (_, set) ->
                        set.map { it.recordNumber }.distinct().toSet()
                    }
            }
            .launchIn(viewModelScope)

        loadAccounts()
            .onEach { newAccounts ->
                accounts.value = newAccounts
                currentAccount.value = findCurrentAccount(newAccounts)
            }
            .launchIn(viewModelScope)

        // Build map of selected contacts per account
        combine(
            accounts.filterNotNull(),
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
                    account.account to selectedContacts.toSet()
                }
            }
        }
            .launchIn(viewModelScope)

        currentAccount
            .filterNotNull()
            .onEach { savedStateHandle[KEY_CURRENT_ACCOUNT] = it.account }
            .launchIn(viewModelScope)

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

    @Suppress("detekt:ReturnCount")
    private fun buildState(
        accounts: List<AccountDisplayModel>,
        currentAccount: AccountDisplayModel?,
        simContacts: List<SimContact>,
        existingContacts: Map<AccountModel, Set<Int>>,
        selectedContacts: Map<AccountModel, Set<Int>>,
    ): State {
        if (accounts.isEmpty() || currentAccount == null) {
            return State.Empty.NoAccounts
        }
        if (simContacts.isEmpty()) {
            return State.Empty.NoContacts
        }

        val (contactsAlreadyImported, contactsToImport) = simContacts.partition { contact ->
            existingContacts.contains(currentAccount, contact)
        }
        return State.Ready(
            accounts = accounts.map(accountUiModelMapper::map).toImmutableList(),
            currentAccount = accountUiModelMapper.map(currentAccount),
            contactsToImport = contactsToImport
                .map {
                    SelectableItem(
                        item = simContactUiModelMapper.map(it),
                        isSelected = selectedContacts.contains(currentAccount, it),
                    )
                }.toImmutableList(),
            contactsAlreadyImported = contactsAlreadyImported
                .map(simContactUiModelMapper::map)
                .toImmutableList(),
        )
    }

    private fun restoreSelectedContacts(): Map<AccountModel, Set<Int>> {
        val entries = savedStateHandle.get<List<AccountContactsEntry>>(KEY_SELECTED_CONTACTS)
        return entries
            ?.associate { it.account to it.contactNumbers.toImmutableSet() }
            ?.toImmutableMap()
            ?: persistentMapOf()
    }

    private fun findCurrentAccount(accounts: List<AccountDisplayModel>): AccountDisplayModel? {
        val currentAccount = currentAccount.value
        return currentAccount
            ?.takeIf { accounts.contains(it) }
            ?: getSavedAccount(accounts)
            ?: getDefaultAccountFromList(accounts)
            ?: accounts.firstOrNull()
    }

    private fun getSavedAccount(accounts: List<AccountDisplayModel>): AccountDisplayModel? {
        return savedStateHandle.get<AccountModel>(KEY_CURRENT_ACCOUNT)?.let { savedAccount ->
            accounts.firstOrNull { it.account == savedAccount }
        }
    }

    private fun getDefaultAccountFromList(
        accounts: List<AccountDisplayModel>,
    ): AccountDisplayModel? {
        return getDefaultAccount()?.let { defaultAccount ->
            accounts.firstOrNull { it.account == defaultAccount }
        }
    }

    private fun close(isSuccessful: Boolean) {
        emitEffect(Effect.Close(isSuccessful))
    }

    private fun changeAccount(account: AccountUiModel) {
        val accounts = accounts.value.orEmpty()
        val newCurrentAccount = accounts.firstOrNull { it.account == account.account } ?: return
        currentAccount.value = newCurrentAccount
    }

    private fun changeContactSelection(contact: SimContactUiModel, isSelected: Boolean) {
        val account = currentAccount.value ?: return
        selectedContacts.update { map ->
            val currentSelectedContacts = map[account.account].orEmpty()
            map + (
                account.account to when {
                    isSelected -> currentSelectedContacts + contact.recordNumber
                    else -> currentSelectedContacts - contact.recordNumber
                }
                )
        }
    }

    private fun selectAll() {
        val account = currentAccount.value ?: return
        val state = (uiState.value as? State.Ready) ?: return
        val allContacts = state.contactsToImport
        selectedContacts.update { map ->
            map + (account.account to allContacts.map { it.item.recordNumber }.toSet())
        }
    }

    private fun deselectAll() {
        val account = currentAccount.value ?: return
        selectedContacts.update { map ->
            map + (account.account to emptySet())
        }
    }

    private fun startImport() {
        val account = currentAccount.value?.account ?: return
        val selectedContacts = getSelectedContacts() ?: run {
            Log.w(TAG, "No selected contacts to import")
            return
        }

        startSimImport(subscriptionId, selectedContacts, account)
        close(isSuccessful = true)
    }

    private fun getSelectedContacts(): List<SimContact>? {
        val allContacts = simContacts.value.orEmpty()
        val state = (uiState.value as? State.Ready) ?: return null
        val selectedContactsNumbers = state.contactsToImport
            .filter { it.isSelected }
            .map { it.item.recordNumber }
        return allContacts
            .filter { selectedContactsNumbers.contains(it.recordNumber) }
            .ifEmpty { null }
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private fun Map<AccountModel, Set<Int>>.contains(
        account: AccountDisplayModel,
        contact: SimContact,
    ) = this[account.account]?.contains(contact.recordNumber) == true

    private companion object {
        const val TAG = "SimImportViewModel"
        const val KEY_CURRENT_ACCOUNT = "accountHeaderSelectedAccount"
        const val KEY_SELECTED_CONTACTS = "selectedContacts"
        const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
