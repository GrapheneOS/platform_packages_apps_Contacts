package com.android.contacts.ui.vcardexport.screen

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.util.IsPermissionGranted
import com.android.contacts.domain.vcard.usecase.CreateTempExportFile
import com.android.contacts.domain.vcard.usecase.ExportVCard
import com.android.contacts.domain.vcard.usecase.GetExportConfig
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayName
import com.android.contacts.ui.vcardexport.screen.model.ExportMode
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardAction as Action
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardEffect as Effect
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardUiState as State
import com.android.contacts.vcard.ExportRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface ExportVCardScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onResume()
    fun onAction(action: Action)
}

@HiltViewModel
internal class ExportVCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getExportConfig: GetExportConfig,
    private val isPermissionGranted: IsPermissionGranted,
    private val createTempExportFile: CreateTempExportFile,
    private val resolveFileDisplayName: ResolveFileDisplayName,
    private val exportVCard: ExportVCard,
) : ViewModel(),
    ExportVCardScreenModel {

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow(
        State(
            availableModes = buildSet {
                val config = getExportConfig()
                if (config.canExportContacts) {
                    add(ExportMode.VCARD_FILE)
                }
                if (config.canShareContacts) {
                    add(ExportMode.SHARE_ALL)
                }
            }.toImmutableSet(),
        ),
    )
    override val uiState = _uiState.asStateFlow()

    private var mode: ExportMode?
        get() = savedStateHandle[KEY_MODE]
        set(value) {
            savedStateHandle[KEY_MODE] = value
        }

    private var hasResumedOnce: Boolean = false

    override fun onResume() {
        if (hasResumedOnce) return
        hasResumedOnce = true

        if (_uiState.value.availableModes.isEmpty()) {
            Log.i(TAG, "No export modes available")
            emitEffect(Effect.Close)
            return
        }

        if (arePermissionsGranted()) {
            onPermissionsGranted()
        } else {
            emitEffect(Effect.RequestPermissions(PERMISSIONS_REQUIRED))
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.PermissionRequestFinished -> onPermissionRequestFinished()
            is Action.ModeSelected -> onModeSelected(action.mode)
            is Action.FileSelected -> onFileSelected(action.uri)
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
        mode?.let {
            onModeSelected(it)
            return
        }
        _uiState.update { it.copy(showModeDialog = true) }
    }

    private fun onModeSelected(mode: ExportMode?) {
        if (mode == null) {
            emitEffect(Effect.Close)
            return
        }

        this.mode = mode
        _uiState.update { it.copy(showModeDialog = false) }
        when (mode) {
            ExportMode.VCARD_FILE -> emitEffect(Effect.SelectFile)
            ExportMode.SHARE_ALL -> viewModelScope.launch {
                val fileUri = createTempExportFile()
                onFileSelected(fileUri)
            }
        }
    }

    private fun onFileSelected(uri: Uri?) {
        if (uri == null) {
            emitEffect(Effect.Close)
            return
        }

        // Start Export

        exportVCard(
            ExportRequest(
                uri,
                null,
                resolveFileDisplayName(uri),
            ),
        )
            .onEach { isSuccessful ->
                if (!isSuccessful) {
                    emitEffect(Effect.ShowError)
                }
            }
            .onCompletion { emitEffect(Effect.Close) }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val TAG = "ExportVCardViewModel"

        @VisibleForTesting
        const val KEY_MODE = "mode"

        @VisibleForTesting
        val PERMISSIONS_REQUIRED = persistentSetOf(
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )
    }
}
