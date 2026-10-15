package com.android.contacts.ui.vcard.cancel.screen

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.vcard.usecase.CancelVCardImportExport
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportAction as Action
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportEffect as Effect
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportUiState as State
import com.android.contacts.ui.vcard.cancel.screen.model.VCardOperationType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface CancelImportExportScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class CancelImportExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cancelVCardImportExport: CancelVCardImportExport,
) : ViewModel(),
    CancelImportExportScreenModel {

    private val data: Uri =
        requireNotNull(savedStateHandle[EXTRA_DATA] as? Uri)
    private val jobId: Int =
        requireNotNull(data.getQueryParameter(QUERY_PARAM_JOB_ID)).toInt()
    private val displayName: String =
        data.getQueryParameter(QUERY_PARAM_DISPLAY_NAME).orEmpty()
    private val type: VCardOperationType =
        mapOperationType(requireNotNull(data.getQueryParameter(QUERY_PARAM_TYPE)))

    private val _uiState = MutableStateFlow<State>(
        State.Confirming(
            type = type,
            displayName = displayName,
        ),
    )
    override val uiState = _uiState.asStateFlow()

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    override fun onAction(action: Action) {
        when (action) {
            Action.CONFIRM -> onCancelConfirm()
            Action.DISMISS -> emitEffect(Effect.CLOSE)
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun onCancelConfirm() {
        viewModelScope.launch {
            val cancelIsSuccessful = cancelVCardImportExport(jobId, displayName)
            when {
                cancelIsSuccessful -> emitEffect(Effect.CLOSE)
                else -> _uiState.value = State.Error
            }
        }
    }

    private fun mapOperationType(type: String): VCardOperationType {
        return VCardOperationType.entries.firstOrNull { it.value == type }
            ?: throw IllegalArgumentException("Invalid VCardService type: $type")
    }

    internal companion object {
        const val EXTRA_DATA = "internal_data"
        const val QUERY_PARAM_JOB_ID = "job_id"
        const val QUERY_PARAM_DISPLAY_NAME = "display_name"
        const val QUERY_PARAM_TYPE = "type"
    }
}
