package com.android.contacts.ui.simimport.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.UIIntents.EXTRA_SUBSCRIPTION_ID
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.simimport.screen.model.SimImportAction as Action
import com.android.contacts.ui.simimport.screen.model.SimImportEffect as Effect
import com.android.contacts.ui.simimport.screen.model.SimImportUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface SimImportScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class SimImportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getDefaultAccount: GetDefaultAccount,
    loadSimContacts: LoadSimContacts,
    loadAccounts: LoadAccounts,
    private val startSimImport: StartSimImport,
) : ViewModel(),
    SimImportScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(State())
    override val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val simContacts =
        MutableStateFlow<List<SimContact>>(emptyList())
    private val existingContacts =
        MutableStateFlow<Map<AccountWithDataSet, Set<Int>>>(emptyMap())
    private val selectedContacts =
        MutableStateFlow<Map<AccountWithDataSet, Set<Int>>>(emptyMap())

    private val subscriptionId: Int = requireNotNull(
        savedStateHandle[EXTRA_SUBSCRIPTION_ID],
    ) { "subscriptionId is required" }

    init {
        loadSimContacts(subscriptionId)
            .onEach { result ->
                simContacts.value = result.contacts
                existingContacts.value = result.existingContactsInAccounts
                    .mapValues { (_, set) -> set.map { it.recordNumber }.distinct().toSet() }
            }
            .launchIn(viewModelScope)

        loadAccounts()
            .onEach { accounts ->
                val defaultAccount = getDefaultAccount()
                    ?.let { AccountInfo.getAccount(accounts, it) }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        accounts = accounts,
                        currentAccount = if (accounts.contains(state.currentAccount)) {
                            state.currentAccount
                        } else {
                            defaultAccount ?: accounts.firstOrNull()
                        },
                    )
                }
            }
            .launchIn(viewModelScope)

        // Build map of selected contacts per account
        combine(
            uiState.map { it.accounts }.distinctUntilChanged(),
            simContacts,
        ) { accounts, contacts ->
            selectedContacts.update { oldSelectedContactsMap ->
                accounts.associate { account ->
                    val oldSelectedContacts = oldSelectedContactsMap[account.account]
                    val selectedContacts = if (oldSelectedContacts.isNullOrEmpty()) {
                        // If we get a new account, select all contacts for import
                        contacts.map { it.recordNumber }
                    } else {
                        // Otherwise let's keep the previousely selected contacts selected
                        val newContactsIds = contacts.map { it.recordNumber }
                        oldSelectedContacts.filter { newContactsIds.contains(it) }
                    }
                    account.account to selectedContacts.toSet()
                }
            }
        }
            .launchIn(viewModelScope)

        // Set SIM contacts to show in the UI according to the current account,
        // and based on whether they are selected or already imported
        combine(
            uiState.map { it.currentAccount },
            simContacts,
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
                    },
                    contactsAlreadyImported = contactsAlreadyImported,
                )
            }
        }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CloseClicked -> close()
            is Action.AccountChanged -> changeAccount(action.account)
            is Action.ContactClicked -> toggleContact(action.contact)
            Action.SelectAllClicked -> selectAll()
            Action.DeselectAllClicked -> deselectAll()
            Action.ImportClicked -> startImport()
        }
    }

    private fun close() {
        emitEffect(Effect.Close)
    }

    private fun changeAccount(account: AccountInfo) {
        _uiState.update { state -> state.copy(currentAccount = account) }
    }

    private fun toggleContact(contact: SimContact) {
        val account = uiState.value.currentAccount ?: return
        selectedContacts.update { map ->
            val currentSelectedContacts = map[account.account].orEmpty()
            map + (
                account.account to
                    if (map.contains(account, contact)) {
                        currentSelectedContacts - contact.recordNumber
                    } else {
                        currentSelectedContacts + contact.recordNumber
                    }
                )
        }
    }

    private fun selectAll() {
        val account = uiState.value.currentAccount ?: return
        val allContacts = uiState.value.contactsToImport
        selectedContacts.update { map ->
            map + (account.account to allContacts.map { it.item.recordNumber }.toSet())
        }
    }

    private fun deselectAll() {
        val account = uiState.value.currentAccount ?: return
        selectedContacts.update { map ->
            map + (account.account to emptySet())
        }
    }

    private fun startImport() {
        val state = uiState.value
        val account = state.currentAccount?.account ?: return
        val contacts = state.contactsToImport.filter { it.isSelected }.map { it.item }
        if (contacts.isEmpty()) return

        startSimImport(subscriptionId, contacts, account)
        close()
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private fun Map<AccountWithDataSet, Set<Int>>.contains(
        account: AccountInfo,
        contact: SimContact,
    ) = this[account.account]?.contains(contact.recordNumber) == true
}
