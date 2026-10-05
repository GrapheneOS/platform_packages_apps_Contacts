package com.android.contacts.ui.interactions.account.filter.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.contacts.model.ContactsCount
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.domain.accounts.usecase.LoadAccountsWithContactsCount
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.interactions.account.filter.AccountFilterActivity
import com.android.contacts.ui.interactions.account.filter.screen.mapper.AccountFilterItemMapper
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterAction as Action
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterEffect as Effect
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterEffect.Close
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterItem
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal interface AccountFilterScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class AccountFilterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    loadAccountsWithContactsCount: LoadAccountsWithContactsCount,
    private val accountFilterItemMapper: AccountFilterItemMapper,
) : ViewModel(),
    AccountFilterScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val selectedFilter: ContactsAccountFilter =
        savedStateHandle[AccountFilterActivity.EXTRA_FILTER]
            ?: ContactsAccountFilter.All

    override val uiState =
        loadAccountsWithContactsCount(AccountFilter.CONTACTS_INSERTABLE)
            .map { State.Ready(items = buildItems(it)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATEFLOW_STOP_TIMEOUT_MILLIS),
                initialValue = State.Loading,
            )

    override fun onAction(action: Action) {
        when (action) {
            Action.Dismiss -> emitEffect(Close())
            is Action.ItemClicked -> emitEffect(Close(action.item.filter))
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private fun buildItems(
        accounts: ContactsCount<AccountDisplayModel>,
    ): ImmutableList<SelectableItem<AccountFilterItem>> {
        val items = accountFilterItemMapper.map(accounts)
        return items.map {
            SelectableItem(
                item = it,
                isSelected = it.filter == selectedFilter,
            )
        }.toImmutableList()
    }

    private companion object {
        const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
