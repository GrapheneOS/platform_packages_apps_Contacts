package com.android.contacts.ui.contactcreation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.R
import com.android.contacts.di.core.DefaultDispatcher
import com.android.contacts.ui.contactcreation.delegate.ContactFieldsDelegate
import com.android.contacts.ui.contactcreation.mapper.RawContactDeltaMapper
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.NameState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class ContactCreationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fieldsDelegate: ContactFieldsDelegate,
    private val deltaMapper: RawContactDeltaMapper,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<ContactCreationUiState>(STATE_KEY) ?: ContactCreationUiState(),
    )
    val uiState: StateFlow<ContactCreationUiState> = _uiState.asStateFlow()

    val nameState: StateFlow<NameState> = _uiState
        .map { it.nameState }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), NameState())

    private val _effects = Channel<ContactCreationEffect>(Channel.BUFFERED)
    val effects: Flow<ContactCreationEffect> = _effects.receiveAsFlow()

    init {
        val restored = savedStateHandle.get<ContactCreationUiState>(STATE_KEY)
        if (restored != null) {
            fieldsDelegate.restorePhones(restored.phoneNumbers)
            fieldsDelegate.restoreEmails(restored.emails)
        }
        viewModelScope.launch {
            _uiState.collect { savedStateHandle[STATE_KEY] = it }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    fun onAction(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.NavigateBack -> handleBack()
            is ContactCreationAction.Save -> save()
            is ContactCreationAction.ConfirmDiscard -> confirmDiscard()

            // Name
            is ContactCreationAction.UpdatePrefix -> updateName { copy(prefix = action.value) }
            is ContactCreationAction.UpdateFirstName -> updateName { copy(first = action.value) }
            is ContactCreationAction.UpdateMiddleName -> updateName { copy(middle = action.value) }
            is ContactCreationAction.UpdateLastName -> updateName { copy(last = action.value) }
            is ContactCreationAction.UpdateSuffix -> updateName { copy(suffix = action.value) }

            // Phone
            is ContactCreationAction.AddPhone ->
                updateState { copy(phoneNumbers = fieldsDelegate.addPhone()) }
            is ContactCreationAction.RemovePhone ->
                updateState { copy(phoneNumbers = fieldsDelegate.removePhone(action.id)) }
            is ContactCreationAction.UpdatePhone ->
                updateState {
                    copy(phoneNumbers = fieldsDelegate.updatePhone(action.id, action.value))
                }
            is ContactCreationAction.UpdatePhoneType ->
                updateState {
                    copy(phoneNumbers = fieldsDelegate.updatePhoneType(action.id, action.type))
                }

            // Email
            is ContactCreationAction.AddEmail ->
                updateState { copy(emails = fieldsDelegate.addEmail()) }
            is ContactCreationAction.RemoveEmail ->
                updateState { copy(emails = fieldsDelegate.removeEmail(action.id)) }
            is ContactCreationAction.UpdateEmail ->
                updateState { copy(emails = fieldsDelegate.updateEmail(action.id, action.value)) }
            is ContactCreationAction.UpdateEmailType ->
                updateState {
                    copy(emails = fieldsDelegate.updateEmailType(action.id, action.type))
                }

            // Photo
            is ContactCreationAction.SetPhoto ->
                updateState { copy(photoUri = action.uri) }
            is ContactCreationAction.RemovePhoto ->
                updateState { copy(photoUri = null) }

            // Account
            is ContactCreationAction.SelectAccount ->
                updateState {
                    copy(
                        selectedAccount = action.account,
                        accountName = action.account.name,
                    )
                }
        }
    }

    fun onSaveResult(success: Boolean, contactUri: Uri?) {
        viewModelScope.launch {
            updateState { copy(isSaving = false) }
            if (success) {
                _effects.send(ContactCreationEffect.SaveSuccess(contactUri))
            } else {
                _effects.send(ContactCreationEffect.ShowError(R.string.contactSavedErrorToast))
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.hasPendingChanges()) return

        viewModelScope.launch(defaultDispatcher) {
            updateState { copy(isSaving = true) }
            val result = deltaMapper.map(state, state.selectedAccount)
            _effects.send(ContactCreationEffect.Save(result))
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            if (_uiState.value.hasPendingChanges()) {
                _effects.send(ContactCreationEffect.ShowDiscardDialog)
            } else {
                _effects.send(ContactCreationEffect.NavigateBack)
            }
        }
    }

    private fun confirmDiscard() {
        viewModelScope.launch {
            _effects.send(ContactCreationEffect.NavigateBack)
        }
    }

    private inline fun updateName(crossinline transform: NameState.() -> NameState) {
        _uiState.update { it.copy(nameState = it.nameState.transform()) }
    }

    private inline fun updateState(
        crossinline transform: ContactCreationUiState.() -> ContactCreationUiState,
    ) {
        _uiState.update { it.transform() }
    }

    internal companion object {
        const val STATE_KEY = "state"
        const val SAVE_COMPLETED_ACTION = "com.android.contacts.SAVE_COMPLETED"
        private const val STOP_TIMEOUT = 5_000L
    }
}
