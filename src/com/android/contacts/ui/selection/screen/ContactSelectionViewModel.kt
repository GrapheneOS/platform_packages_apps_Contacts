package com.android.contacts.ui.selection.screen

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.domain.contacts.usecase.GetContact
import com.android.contacts.domain.contacts.usecase.GetContactsForSelection
import com.android.contacts.ui.selection.ContactSelectionActivity
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionEffectMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionFilterMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionUiStateMapper
import com.android.contacts.ui.selection.screen.model.ContactSelectionAction as Action
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionState as State
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState as UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch

internal interface ContactSelectionScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<UiState>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ContactSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContactsForSelection: GetContactsForSelection,
    private val getContact: GetContact,
    private val filterMapper: ContactSelectionFilterMapper,
    uiStateMapper: ContactSelectionUiStateMapper,
    private val effectMapper: ContactSelectionEffectMapper,
) : ViewModel(),
    ContactSelectionScreenModel {

    private val request: ContactSelectionRequest? =
        savedStateHandle[ContactSelectionActivity.EXTRA_REQUEST]

    private val mode: ContactSelectionMode = request?.mode ?: Mode.Pick()

    private val state = savedStateHandle.getMutableStateFlow(KEY_STATE, buildInitialState())

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private var isSubmitting: Boolean = false

    override val uiState: StateFlow<UiState> =
        combine(
            flowOf(request).filterNotNull(),
            state,
            getContacts(),
            getJoinTarget(),
            uiStateMapper::map,
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS),
                initialValue = UiState(),
            )

    init {
        if (request == null) {
            emitEffect(Effect.Close(Result.Canceled))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getContacts(): Flow<Pair<String?, GetContactsForSelectionResult>?> {
        val filter = filterMapper.map(mode)
        return state
            .map { it.searchState.effectiveQuery }
            .debounceSearch()
            .distinctUntilChanged()
            .flatMapLatest { searchQuery ->
                getContactsForSelection(filter, searchQuery)
                    .map<_, Pair<String?, GetContactsForSelectionResult>?> { searchQuery to it }
            }
            .retainLatestState()
    }

    private fun getJoinTarget(): Flow<ContactItem?> {
        return when {
            mode is Mode.Join -> {
                flow<ContactItem?> { emit(getContact(mode.targetContactId).first()) }
                    .retainLatestState()
            }
            else -> flowOf(null)
        }
    }

    private fun buildInitialState(): State {
        return State(
            searchState = when {
                !mode.isSearchSupported -> SearchState.Unavailable
                mode is Mode.Pick && mode.searchQuery != null ->
                    SearchState.Searching(mode.searchQuery)
                else -> SearchState.Regular
            },
            selectedKeys = when (mode) {
                is Mode.SendToGroup -> mode.defaultSelection.mapTo(mutableSetOf()) {
                    ContactItemKey.Local(it)
                }
                else -> emptySet()
            },
        )
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CloseClick -> onCloseClick()
            is Action.ContactClick -> onContactClick(action.key)
            Action.SearchClick -> onSearchClick()
            is Action.SearchQueryChange -> onSearchQueryChange(action.query)
            Action.CreateClick -> onCreateClick()
            Action.SubmitClick -> onSubmitClick()
        }
    }

    private fun onCloseClick() {
        when (state.value.searchState) {
            is SearchState.Searching ->
                state.update { it.copy(searchState = SearchState.Regular) }
            else ->
                emitEffect(Effect.Close(Result.Canceled))
        }
    }

    private fun onContactClick(key: ContactItemKey) {
        when {
            // Remote contacts can't be multi-selected, so they're returned right away
            mode.isMultiple && key is ContactItemKey.Local -> {
                state.update { state ->
                    val selectedKeys = state.selectedKeys
                    state.copy(
                        selectedKeys = when {
                            selectedKeys.contains(key) -> selectedKeys - key
                            else -> selectedKeys + key
                        },
                    )
                }
            }
            else -> {
                finishWithResult(setOf(key))
            }
        }
    }

    private fun onSearchClick() {
        state.update { state ->
            state.copy(searchState = SearchState.Searching(""))
        }
    }

    private fun onSearchQueryChange(newQuery: String) {
        state.update { state ->
            state.copy(searchState = SearchState.Searching(newQuery))
        }
    }

    private fun onCreateClick() {
        emitEffect(Effect.ForwardToEditor.CreateContact)
    }

    private fun onSubmitClick() {
        finishWithResult(state.value.selectedKeys)
    }

    private fun finishWithResult(keys: Set<ContactItemKey>) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            emitEffect(effectMapper.map(mode, keys))
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun <T> Flow<T?>.retainLatestState(): StateFlow<T?> {
        return stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )
    }

    @OptIn(FlowPreview::class)
    private fun Flow<String?>.debounceSearch(): Flow<String?> {
        return withIndex()
            .debounce { (index, query) ->
                when {
                    index == 0 || query == null -> 0L
                    else -> SEARCH_DEBOUNCE_MILLIS
                }
            }
            .map { it.value }
    }

    companion object {
        private const val KEY_STATE = "state"

        @VisibleForTesting
        const val STATE_TIMEOUT_MILLIS = 5_000L

        @VisibleForTesting
        const val SEARCH_DEBOUNCE_MILLIS = 200L
    }
}
