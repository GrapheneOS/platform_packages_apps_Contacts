package com.android.contacts.ui.vcard.screen

import android.Manifest
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.util.IsPermissionGranted
import com.android.contacts.domain.vcard.model.ImportVCardSource as Source
import com.android.contacts.domain.vcard.usecase.BuildVCardSource
import com.android.contacts.domain.vcard.usecase.ImportVCards
import com.android.contacts.ui.vcard.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcard.screen.model.ImportVCardEffect as Effect
import com.android.contacts.ui.vcard.screen.model.ImportVCardUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface ImportVCardScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onResume()
    fun onAction(action: Action)
}

@HiltViewModel
internal class ImportVCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val isPermissionGranted: IsPermissionGranted,
    private val buildVCardSource: BuildVCardSource,
    private val importVCards: ImportVCards,
) : ViewModel(),
    ImportVCardScreenModel {

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<State>(State.Preparing)
    override val uiState = _uiState.asStateFlow()

    private var hasResumedOnce: Boolean = false
    private var importJob: Job? = null

    private var sources: List<Source>?
        get() = savedStateHandle[KEY_SOURCES]
        set(value) {
            savedStateHandle[KEY_SOURCES] = value
        }

    private var account: AccountModel?
        get() = savedStateHandle[KEY_ACCOUNT]
        set(value) {
            savedStateHandle[KEY_ACCOUNT] = value
        }

    override fun onResume() {
        if (hasResumedOnce) return
        hasResumedOnce = true
        if (arePermissionsGranted()) {
            onPermissionsGranted()
        } else {
            emitEffect(Effect.RequestPermissions(PERMISSIONS_REQUIRED))
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.PermissionRequestFinished -> onPermissionRequestFinished()
            is Action.FilesSelected -> onFilesSelected(action.uris)
            is Action.AccountSelected -> onAccountSelected(action.account)
            Action.CancelClicked -> {
                if (importJob != null) {
                    importJob?.cancel()
                } else {
                    emitEffect(Effect.Close)
                }
            }
            Action.FailureDismissed -> emitEffect(Effect.Close)
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun arePermissionsGranted(): Boolean {
        return PERMISSIONS_REQUIRED.all { isPermissionGranted(it) }
    }

    private fun onPermissionRequestFinished() {
        if (arePermissionsGranted()) {
            onPermissionsGranted()
        } else {
            emitEffect(Effect.Close)
        }
    }

    private fun onPermissionsGranted() {
        viewModelScope.launch {
            savedStateHandle.get<Uri>(KEY_INITIAL_FILE)?.let { initialFileUriReceived ->
                sources = buildSources(listOf(initialFileUriReceived))
                savedStateHandle[KEY_INITIAL_FILE] = null
            }

            if (sources != null) {
                onSources()
            } else {
                emitEffect(Effect.SelectFiles)
            }
        }
    }

    private fun onFilesSelected(uris: List<Uri>?) {
        if (uris.isNullOrEmpty()) {
            emitEffect(Effect.Close)
            return
        }

        viewModelScope.launch {
            sources = buildSources(uris)
            onSources()
        }
    }

    private fun onSources() {
        if (sources?.isEmpty() != false) {
            emitEffect(Effect.Close)
            return
        }

        account
            ?.let(::onAccountSelected)
            ?: emitEffect(Effect.SelectAccount)
    }

    private fun onAccountSelected(account: AccountModel?) {
        if (account == null) {
            emitEffect(Effect.Close)
            return
        }
        this.account = account
        startImport()
    }

    private suspend fun buildSources(uris: List<Uri>): List<Source> {
        return uris.mapNotNull { buildVCardSource(it) }
    }

    private fun startImport() {
        if (importJob != null) return
        val sources = sources
        val account = account
        if (sources == null || account == null) return
        _uiState.value = State.Importing

        importJob = importVCards(account, sources)
            .onEach { emitEffect(Effect.ShowImportError(it)) }
            .onCompletion { emitEffect(Effect.Close) }
            .launchIn(viewModelScope)
        importJob = null
    }

    companion object {
        const val KEY_INITIAL_FILE = "initial_file"

        @VisibleForTesting
        const val KEY_SOURCES = "sources"

        @VisibleForTesting
        const val KEY_ACCOUNT = "account"

        @VisibleForTesting
        val PERMISSIONS_REQUIRED = persistentSetOf(
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )
    }
}
