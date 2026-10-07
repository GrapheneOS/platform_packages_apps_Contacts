package com.android.contacts.ui.vcardexport.screen

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.vcard.usecase.CreateTempExportFile
import com.android.contacts.domain.vcard.usecase.ExportVCard
import com.android.contacts.domain.vcard.usecase.GetExportConfig
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayName
import com.android.contacts.ui.vcardexport.screen.model.ExportMode
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardAction as Action
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardEffect as Effect
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardUiState as State
import com.android.contacts.util.core.IsPermissionGranted
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
    savedStateHandle: SavedStateHandle,
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

    private val step = savedStateHandle.getMutableStateFlow(KEY_STEP, Step.START)

    override fun onResume() {
        if (_uiState.value.availableModes.isEmpty()) {
            Log.i(TAG, "No export modes available")
            emitEffect(Effect.Close)
            return
        }

        step
            .onEach { step ->
                when (step) {
                    Step.START -> {
                        this.step.value = when {
                            !arePermissionsGranted() -> Step.REQUESTING_PERMISSIONS
                            else -> Step.SELECTING_MODE
                        }
                    }
                    Step.REQUESTING_PERMISSIONS -> {
                        emitEffect(Effect.RequestPermissions(PERMISSIONS_REQUIRED))
                    }
                    Step.SELECTING_MODE -> {
                        _uiState.update { it.copy(showModeDialog = true) }
                    }
                    Step.SELECTING_FILE -> {
                        _uiState.update { it.copy(showModeDialog = false) }
                        emitEffect(Effect.SelectFile)
                    }
                    Step.EXPORTING -> {
                        _uiState.update { it.copy(showModeDialog = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.PermissionRequestFinished -> onPermissionRequestFinished()
            is Action.ModeSelected -> onModeSelected(action.mode)
            is Action.FileSelected -> startExport(action.uri)
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun arePermissionsGranted(): Boolean {
        return PERMISSIONS_REQUIRED.all { isPermissionGranted(it) }
    }

    private fun onPermissionRequestFinished() {
        when {
            step.value != Step.REQUESTING_PERMISSIONS -> return
            !arePermissionsGranted() -> emitEffect(Effect.Close)
            else -> step.value = Step.SELECTING_MODE
        }
    }

    private fun onModeSelected(mode: ExportMode?) {
        if (step.value != Step.SELECTING_MODE || mode == null) {
            emitEffect(Effect.Close)
            return
        }

        when (mode) {
            ExportMode.VCARD_FILE -> {
                step.value = Step.SELECTING_FILE
            }
            ExportMode.SHARE_ALL -> viewModelScope.launch {
                val fileUri = createTempExportFile()
                startExport(fileUri)
            }
        }
    }

    private fun startExport(fileUri: Uri?) {
        if (fileUri == null) {
            emitEffect(Effect.Close)
            return
        }

        step.value = Step.EXPORTING
        exportVCard(
            ExportRequest(
                fileUri,
                null,
                resolveFileDisplayName(fileUri),
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

    @VisibleForTesting
    enum class Step {
        START,
        REQUESTING_PERMISSIONS,
        SELECTING_MODE,
        SELECTING_FILE,
        EXPORTING,
    }

    companion object {
        private const val TAG = "ExportVCardViewModel"

        @VisibleForTesting
        const val KEY_STEP = "step"

        @VisibleForTesting
        val PERMISSIONS_REQUIRED = persistentSetOf(
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )
    }
}
