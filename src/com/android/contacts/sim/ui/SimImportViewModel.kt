package com.android.contacts.sim.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.sim.LoadSimContacts
import kotlin.collections.map
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SimImportViewModel(
    subscriptionId: Int,
    getDefaultAccount: () -> AccountWithDataSet?,
    loadSimContacts: (Int) -> Flow<LoadSimContacts.Result>,
    loadAccounts: () -> Flow<List<AccountInfo>>,
    startSimImport: (Int, List<SimContact>, AccountWithDataSet) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val events =
        MutableSharedFlow<Event>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    fun onEvent(event: Event) = events.tryEmit(event)

    private val simContacts =
        MutableStateFlow<List<SimContact>>(emptyList())
    private val existingContacts =
        MutableStateFlow<Map<AccountWithDataSet, Set<Int>>>(emptyMap())
    private val selectedContacts =
        MutableStateFlow<Map<AccountWithDataSet, Set<Int>>>(emptyMap())

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

                _state.update { state ->
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
            state.map { it.accounts }.distinctUntilChanged(),
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
            state.map { it.currentAccount },
            simContacts,
            selectedContacts,
            existingContacts,
        ) { account, simContacts, selectedContacts, existingContacts ->
            val account = account ?: return@combine
            _state.update { state ->
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

        events.filterIsInstance<Event.AccountChanged>()
            .onEach { _state.update { state -> state.copy(currentAccount = it.account) } }
            .launchIn(viewModelScope)

        events.filterIsInstance<Event.ContactClicked>()
            .onEach { (contact) ->
                val account = state.value.currentAccount ?: return@onEach
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
            .launchIn(viewModelScope)

        events.filterIsInstance<Event.SelectAllClicked>()
            .onEach {
                val account = state.value.currentAccount ?: return@onEach
                val allContacts = state.value.contactsToImport
                selectedContacts.update { map ->
                    map + (account.account to allContacts.map { it.item.recordNumber }.toSet())
                }
            }
            .launchIn(viewModelScope)

        events.filterIsInstance<Event.DeselectAllClicked>()
            .onEach {
                val account = state.value.currentAccount ?: return@onEach
                selectedContacts.update { map ->
                    map + (account.account to emptySet())
                }
            }
            .launchIn(viewModelScope)

        events.filterIsInstance<Event.ImportClicked>()
            .onEach {
                _state.update { state ->
                    val account = state.currentAccount?.account ?: return@update state
                    val contacts = state.contactsToImport.filter { it.isSelected }.map { it.item }
                    if (contacts.isEmpty()) return@update state

                    startSimImport(subscriptionId, contacts, account)
                    state.copy(close = true)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun Map<AccountWithDataSet, Set<Int>>.contains(
        account: AccountInfo,
        contact: SimContact,
    ) =
        this[account.account]?.contains(contact.recordNumber) == true

    data class State(
        val isLoading: Boolean = true,
        val accounts: List<AccountInfo> = emptyList(),
        val currentAccount: AccountInfo? = null,
        val contactsToImport: List<SelectableItem<SimContact>> = emptyList(),
        val contactsAlreadyImported: List<SimContact> = emptyList(),
        val close: Boolean = false,
    ) {
        val selectedContactsCount = contactsToImport.count { it.isSelected }
        val isImportEnabled get() = selectedContactsCount > 0
        val isSelectAllEnabled get() = selectedContactsCount != contactsToImport.size
        val isDeselectAllEnabled get() = selectedContactsCount != 0
    }

    sealed interface Event {
        data class AccountChanged(val account: AccountInfo) : Event
        data class ContactClicked(val contact: SimContact) : Event
        data object SelectAllClicked : Event
        data object DeselectAllClicked : Event
        data object ImportClicked : Event
    }
}
