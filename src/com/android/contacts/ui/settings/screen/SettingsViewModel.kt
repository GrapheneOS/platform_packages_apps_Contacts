package com.android.contacts.ui.settings.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.data.profile.repository.ProfileRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.simimport.model.SimImportResult
import com.android.contacts.data.simimport.repository.SimImportResultRepository
import com.android.contacts.domain.settings.model.SettingsData
import com.android.contacts.domain.settings.usecase.GetSettingsData
import com.android.contacts.ui.settings.screen.mapper.SettingsUiStateMapper
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal interface SettingsScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<SettingsUiState>

    fun refreshState()
    fun onAction(action: Action)
}

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val getSettingsData: GetSettingsData,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val settingsUiStateMapper: SettingsUiStateMapper,
    simImportResultRepository: SimImportResultRepository,
    profileRepository: ProfileRepository,
) : ViewModel(),
    SettingsScreenModel {

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    override val effects: Flow<Effect> = _effects.asSharedFlow()

    private val refreshTriggers = Channel<Unit>(Channel.CONFLATED)

    private val settingsData: Flow<SettingsData> = refreshTriggers.receiveAsFlow()
        .onStart { emit(Unit) }
        .map { getSettingsData() }

    private val profile: StateFlow<ProfileData?> = profileRepository.observeProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override val uiState: StateFlow<SettingsUiState> = combine(
        settingsData,
        profile,
    ) { settingsData, profile ->
        settingsUiStateMapper.map(
            settingsData = settingsData,
            profile = profile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS),
        initialValue = SettingsUiState(),
    )

    init {
        simImportResultRepository.observeSimImportResults()
            .onEach { result -> emitEffect(toEffect(result)) }
            .launchIn(viewModelScope)
    }

    override fun refreshState() {
        refreshTriggers.trySend(Unit)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.LicensesClicked -> emitEffect(Effect.OpenLicenses)

            is Action.ItemClicked -> {
                onItemClicked(action.id)
            }

            is Action.SortOrderSelected -> {
                selectSortOrder(action.sortOrder)
            }
            is Action.DisplayOrderSelected -> {
                selectDisplayOrder(action.displayOrder)
            }

            is Action.PhoneticNameDisplaySelected -> {
                selectPhoneticNameDisplay(action.phoneticNameDisplay)
            }
        }
    }

    private fun onItemClicked(id: SettingsItemId) {
        when (id) {
            SettingsItemId.MY_INFO -> openProfile()
            SettingsItemId.ACCOUNTS -> emitEffect(Effect.OpenAddAccount)
            SettingsItemId.DEFAULT_ACCOUNT -> emitEffect(Effect.OpenDefaultAccountPicker)
            SettingsItemId.CONTACTS_FILTER -> emitEffect(Effect.OpenContactsFilter)
            SettingsItemId.IMPORT -> emitEffect(Effect.ShowImportDialog)
            SettingsItemId.EXPORT -> emitEffect(Effect.ShowExportDialog)
            SettingsItemId.BLOCKED_NUMBERS -> emitEffect(Effect.OpenBlockedNumbers)

            SettingsItemId.SORT_ORDER,
            SettingsItemId.DISPLAY_ORDER,
            SettingsItemId.PHONETIC_NAME_DISPLAY,
            SettingsItemId.ABOUT,
            -> Unit
        }
    }

    private fun openProfile() {
        val contactId = profile.value
            ?.takeIf { it.hasProfile }
            ?.contactId

        val effect = when (contactId) {
            null -> Effect.CreateProfile
            else -> Effect.OpenProfile(contactId)
        }

        emitEffect(effect)
    }

    private fun selectSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            displaySettingsRepository.setSortOrder(sortOrder)
            refreshState()
        }
    }

    private fun selectDisplayOrder(displayOrder: DisplayOrder) {
        viewModelScope.launch {
            displaySettingsRepository.setDisplayOrder(displayOrder)
            refreshState()
        }
    }

    private fun selectPhoneticNameDisplay(phoneticNameDisplay: PhoneticNameDisplay) {
        viewModelScope.launch {
            displaySettingsRepository.setPhoneticNameDisplay(phoneticNameDisplay)
            refreshState()
        }
    }

    private fun toEffect(result: SimImportResult): Effect {
        return when (result) {
            is SimImportResult.Success -> Effect.ShowSimImportSuccess(result.importedCount)
            is SimImportResult.Failure -> Effect.ShowSimImportFailure
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }

    private companion object {
        const val STATE_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
