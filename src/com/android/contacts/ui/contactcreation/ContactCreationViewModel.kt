package com.android.contacts.ui.contactcreation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
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

@Suppress("TooManyFunctions")
@HiltViewModel
internal class ContactCreationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fieldsDelegate: ContactFieldsDelegate,
    private val deltaMapper: RawContactDeltaMapper,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ApplicationContext private val appContext: Context,
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
            fieldsDelegate.restoreAddresses(restored.addresses)
            fieldsDelegate.restoreEvents(restored.events)
            fieldsDelegate.restoreRelations(restored.relations)
            fieldsDelegate.restoreImAccounts(restored.imAccounts)
            fieldsDelegate.restoreWebsites(restored.websites)
            fieldsDelegate.restoreGroups(restored.groups)
        }
        viewModelScope.launch {
            _uiState.collect { savedStateHandle[STATE_KEY] = it }
        }
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
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

            // Address
            is ContactCreationAction.AddAddress ->
                updateState { copy(addresses = fieldsDelegate.addAddress()) }
            is ContactCreationAction.RemoveAddress ->
                updateState { copy(addresses = fieldsDelegate.removeAddress(action.id)) }
            is ContactCreationAction.UpdateAddressStreet ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressStreet(action.id, action.value))
                }
            is ContactCreationAction.UpdateAddressCity ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressCity(action.id, action.value))
                }
            is ContactCreationAction.UpdateAddressRegion ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressRegion(action.id, action.value))
                }
            is ContactCreationAction.UpdateAddressPostcode ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressPostcode(action.id, action.value))
                }
            is ContactCreationAction.UpdateAddressCountry ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressCountry(action.id, action.value))
                }
            is ContactCreationAction.UpdateAddressType ->
                updateState {
                    copy(addresses = fieldsDelegate.updateAddressType(action.id, action.type))
                }

            // Organization
            is ContactCreationAction.UpdateCompany ->
                updateState { copy(organization = organization.copy(company = action.value)) }
            is ContactCreationAction.UpdateJobTitle ->
                updateState { copy(organization = organization.copy(title = action.value)) }

            // Event
            is ContactCreationAction.AddEvent ->
                updateState { copy(events = fieldsDelegate.addEvent()) }
            is ContactCreationAction.RemoveEvent ->
                updateState { copy(events = fieldsDelegate.removeEvent(action.id)) }
            is ContactCreationAction.UpdateEvent ->
                updateState {
                    copy(events = fieldsDelegate.updateEvent(action.id, action.value))
                }
            is ContactCreationAction.UpdateEventType ->
                updateState {
                    copy(events = fieldsDelegate.updateEventType(action.id, action.type))
                }

            // Relation
            is ContactCreationAction.AddRelation ->
                updateState { copy(relations = fieldsDelegate.addRelation()) }
            is ContactCreationAction.RemoveRelation ->
                updateState { copy(relations = fieldsDelegate.removeRelation(action.id)) }
            is ContactCreationAction.UpdateRelation ->
                updateState {
                    copy(relations = fieldsDelegate.updateRelation(action.id, action.value))
                }
            is ContactCreationAction.UpdateRelationType ->
                updateState {
                    copy(relations = fieldsDelegate.updateRelationType(action.id, action.type))
                }

            // IM
            is ContactCreationAction.AddIm ->
                updateState { copy(imAccounts = fieldsDelegate.addIm()) }
            is ContactCreationAction.RemoveIm ->
                updateState { copy(imAccounts = fieldsDelegate.removeIm(action.id)) }
            is ContactCreationAction.UpdateIm ->
                updateState {
                    copy(imAccounts = fieldsDelegate.updateIm(action.id, action.value))
                }
            is ContactCreationAction.UpdateImProtocol ->
                updateState {
                    copy(
                        imAccounts = fieldsDelegate.updateImProtocol(
                            action.id,
                            action.protocol,
                        ),
                    )
                }

            // Website
            is ContactCreationAction.AddWebsite ->
                updateState { copy(websites = fieldsDelegate.addWebsite()) }
            is ContactCreationAction.RemoveWebsite ->
                updateState { copy(websites = fieldsDelegate.removeWebsite(action.id)) }
            is ContactCreationAction.UpdateWebsite ->
                updateState {
                    copy(websites = fieldsDelegate.updateWebsite(action.id, action.value))
                }
            is ContactCreationAction.UpdateWebsiteType ->
                updateState {
                    copy(websites = fieldsDelegate.updateWebsiteType(action.id, action.type))
                }

            // Note
            is ContactCreationAction.UpdateNote ->
                updateState { copy(note = action.value) }

            // Nickname
            is ContactCreationAction.UpdateNickname ->
                updateState { copy(nickname = action.value) }

            // SIP
            is ContactCreationAction.UpdateSipAddress ->
                updateState { copy(sipAddress = action.value) }

            // Groups
            is ContactCreationAction.ToggleGroup ->
                updateState {
                    copy(groups = fieldsDelegate.toggleGroup(action.groupId, action.title))
                }

            // More fields
            is ContactCreationAction.ToggleMoreFields ->
                updateState { copy(isMoreFieldsExpanded = !isMoreFieldsExpanded) }

            // Photo
            is ContactCreationAction.SetPhoto ->
                updateState { copy(photoUri = action.uri) }
            is ContactCreationAction.RemovePhoto ->
                updateState { copy(photoUri = null) }
            is ContactCreationAction.RequestGallery ->
                viewModelScope.launch { _effects.send(ContactCreationEffect.LaunchGallery) }
            is ContactCreationAction.RequestCamera -> requestCamera()

            // Account
            is ContactCreationAction.SelectAccount ->
                updateState {
                    copy(
                        selectedAccount = action.account,
                        accountName = action.account.name,
                        groups = fieldsDelegate.clearGroups(),
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

    private fun requestCamera() {
        viewModelScope.launch(defaultDispatcher) {
            val photoDir = File(appContext.cacheDir, PHOTO_CACHE_DIR).apply { mkdirs() }
            val photoFile = File(photoDir, "photo_${UUID.randomUUID()}.jpg")
            val authority = appContext.getString(R.string.contacts_file_provider_authority)
            val uri = FileProvider.getUriForFile(appContext, authority, photoFile)
            pendingCameraUri = uri
            _effects.send(ContactCreationEffect.LaunchCamera(uri))
        }
    }

    /** URI of the file passed to ACTION_IMAGE_CAPTURE, awaiting result. */
    private var pendingCameraUri: Uri? = null

    fun getPendingCameraUri(): Uri? = pendingCameraUri

    fun clearPendingCameraUri() {
        pendingCameraUri = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanupTempPhotos()
    }

    private fun cleanupTempPhotos() {
        val photoDir = File(appContext.cacheDir, PHOTO_CACHE_DIR)
        if (photoDir.exists()) {
            photoDir.listFiles()?.forEach { it.delete() }
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
        private const val PHOTO_CACHE_DIR = "contact_photos"
    }
}
