package com.android.contacts.ui.group.list.screen

import android.content.ContentUris
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.ui.group.list.GroupsActivity
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapper
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import com.android.contacts.ui.group.list.screen.model.GroupsAction as Action
import com.android.contacts.ui.group.list.screen.model.GroupsEffect as Effect
import com.android.contacts.ui.group.list.screen.model.GroupsUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

internal interface GroupsScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class GroupsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGroups: GetGroups,
    private val groupsUiMapper: GroupsUiMapper,
    private val getDefaultAccount: GetDefaultAccount,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
) : ViewModel(),
    GroupsScreenModel {

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val account: AccountModel? =
        savedStateHandle.get<AccountModel>(GroupsActivity.EXTRA_ACCOUNT)

    override val uiState: StateFlow<State> =
        combine(
            getGroups(
                filters = groupFilters(),
                sort = GroupSort.BY_TITLE,
            ),
            loadDefaultAccount(),
            ::buildState,
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATEFLOW_STOP_TIMEOUT_MILLIS),
                initialValue = State.Loading,
            )

    private fun buildState(
        groups: List<Group>?,
        defaultAccount: AccountDisplayModel?,
    ): State {
        return when {
            groups == null -> {
                Log.w(TAG, "Failed to load groups")
                emitEffect(Effect.Close)
                State.Loading
            }
            groups.isEmpty() && defaultAccount == null -> {
                State.WithoutAccounts
            }
            else -> {
                val accountGroups = when {
                    groups.isEmpty() -> persistentListOf(groupsUiMapper.map(defaultAccount))
                    else -> groupsUiMapper.map(groups)
                }
                val onlyOneNullAccount = accountGroups.all { it.account == null }
                State.WithAccounts(
                    groups = accountGroups,
                    showAccountHeaders = account == null && !onlyOneNullAccount,
                )
            }
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.Dismissed -> close()
            is Action.GroupClicked -> onGroupClick(action.group)
            is Action.NewClicked -> onNewClick(action.account)
        }
    }

    private fun groupFilters(): List<GroupFilter> {
        return listOfNotNull(
            account?.let(GroupFilter::ByAccount),
            GroupFilter.AutoAdd(false),
            GroupFilter.Favorites(false),
        )
    }

    private fun loadDefaultAccount(): Flow<AccountDisplayModel?> {
        return flowOf(
            getDefaultAccount()
                ?.let(accountDisplayModelMapper::map),
        )
    }

    private fun close() {
        emitEffect(Effect.Close)
    }

    private fun onGroupClick(group: GroupUiItem) {
        val groupUri = ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, group.id)
        emitEffect(Effect.OpenGroup(groupUri))
    }

    private fun onNewClick(account: AccountModel?) {
        emitEffect(Effect.CreateNewGroup(account))
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private companion object {
        const val TAG = "GroupsViewModel"
        const val STATEFLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
