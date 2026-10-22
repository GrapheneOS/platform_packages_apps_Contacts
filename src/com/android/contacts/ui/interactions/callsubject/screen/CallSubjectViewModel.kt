package com.android.contacts.ui.interactions.callsubject.screen

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.callsubject.repository.CallSubjectRepository
import com.android.contacts.data.callsubject.repository.GetCallSubjectConfiguration
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectAction as Action
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectArguments
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectEffect as Effect
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface CallSubjectScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class CallSubjectViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getCallSubjectConfiguration: GetCallSubjectConfiguration,
    private val callSubjectRepository: CallSubjectRepository,
) : ViewModel(),
    CallSubjectScreenModel {

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val args: CallSubjectArguments =
        requireNotNull(savedStateHandle[EXTRA_ARGUMENTS])

    private var subject: String
        get() = savedStateHandle[KEY_SUBJECT] ?: ""
        set(value) {
            savedStateHandle[KEY_SUBJECT] = value
        }

    private var isCalling: Boolean = false

    private val _uiState = MutableStateFlow(
        State(
            avatarImage = args.thumbnailPhotoUri?.let(ContactAvatarImage::Uri),
            isBusiness = args.isBusiness,
            name = args.nameOrNumber,
            number = args.displayNumber,
            numberLabel = args.numberLabel,
            subject = subject,
        ),
    )
    override val uiState: StateFlow<State> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = getCallSubjectConfiguration(args.phoneAccountHandle)
            _uiState.update {
                it.copy(
                    subjectLimit = config.limit,
                    subjectEncoding = config.encoding,
                )
            }
        }

        viewModelScope.launch {
            val history = callSubjectRepository.getHistory().reversed()
            _uiState.update {
                it.copy(history = history.toImmutableList())
            }
        }

        uiState
            .map { it.subject }
            .distinctUntilChanged()
            .onEach { subject = it }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CallClick -> onCallClick()
            Action.Dismiss -> onDismiss()
            is Action.HistorySubjectPick -> onHistoryPick(action.subject)
            is Action.SubjectChange -> onSubjectChange(action)
        }
    }

    private fun onCallClick() {
        if (isCalling) return
        isCalling = true
        val subject = uiState.value.subject
        viewModelScope.launch {
            callSubjectRepository.addToHistory(subject)
            emitEffect(
                Effect.PlaceCall(
                    subject = subject,
                    number = args.number,
                    phoneAccountHandle = args.phoneAccountHandle,
                ),
            )
        }
    }

    private fun onDismiss() {
        emitEffect(Effect.Close)
    }

    private fun onHistoryPick(subject: String) {
        _uiState.update { it.copy(subject = subject) }
    }

    private fun onSubjectChange(action: Action.SubjectChange) {
        _uiState.update { it.copy(subject = action.value) }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    companion object {
        const val EXTRA_ARGUMENTS = "arguments"

        @VisibleForTesting
        const val KEY_SUBJECT = "subject"
    }
}
