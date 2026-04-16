package com.android.contacts.ui.contactcreation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.R
import com.android.contacts.di.core.DefaultDispatcher
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.mapper.RawContactDeltaMapper
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationEffect
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// TooManyFunctions: MVI ViewModels inherently have many functions -- one dispatcher (onAction),
// plus private handlers for each action group, plus lifecycle/save/state helpers. This count
// is proportional to the number of contact field types and cannot be reduced without degrading
// readability or moving to a less explicit dispatch mechanism.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class ContactCreationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val deltaMapper: RawContactDeltaMapper,
    private val accountTypeManager: AccountTypeManager,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<ContactCreationUiState>(STATE_KEY) ?: ContactCreationUiState(),
    )
    val uiState: StateFlow<ContactCreationUiState> = _uiState.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountWithDataSet>>(emptyList())
    val accounts: StateFlow<List<AccountWithDataSet>> = _accounts.asStateFlow()

    private val _effects = Channel<ContactCreationEffect>(Channel.BUFFERED)
    val effects: Flow<ContactCreationEffect> = _effects.receiveAsFlow()

    init {
        cleanupTempPhotos()
        loadWritableAccounts()

        viewModelScope.launch {
            _uiState.collect { savedStateHandle[STATE_KEY] = it }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loadWritableAccounts() {
        viewModelScope.launch(defaultDispatcher) {
            try {
                val filter = AccountTypeManager.insertableFilter(appContext)
                val loaded = accountTypeManager.filterAccountsAsync(filter)
                    .get(ACCOUNT_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .map { it.account }
                _accounts.value = loaded
                if (_uiState.value.selectedAccount == null) {
                    loaded.firstOrNull()?.let { first ->
                        updateState {
                            copy(selectedAccount = first, accountName = first.name)
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback: device-only, empty account list
            }
        }
    }

    fun onAction(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.NavigateBack -> handleBack()
            is ContactCreationAction.Save -> save()
            is ContactCreationAction.ConfirmDiscard -> confirmDiscard()
            is ContactCreationAction.DismissDiscardDialog -> dismissDiscardDialog()
            is ContactCreationAction.SelectAccount -> handleSelectAccount(action)
            else -> handleSectionToggleOrFieldUpdate(action)
        }
    }

    private fun handleSelectAccount(action: ContactCreationAction.SelectAccount) {
        val writable = _accounts.value
        if (writable.isEmpty() || action.account in writable) {
            updateState {
                copy(
                    selectedAccount = action.account,
                    accountName = action.account.name,
                    groups = emptyList(),
                )
            }
        }
    }

    private fun handleSectionToggleOrFieldUpdate(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.ShowOrganization ->
                updateState { copy(showOrganization = true) }
            is ContactCreationAction.HideOrganization ->
                updateState {
                    copy(showOrganization = false, organization = OrganizationFieldState())
                }
            is ContactCreationAction.ShowNote -> updateState { copy(showNote = true) }
            is ContactCreationAction.HideNote -> updateState { copy(showNote = false, note = "") }
            is ContactCreationAction.ShowNickname -> updateState { copy(showNickname = true) }
            is ContactCreationAction.HideNickname ->
                updateState { copy(showNickname = false, nickname = "") }
            is ContactCreationAction.ShowSipAddress ->
                updateState { copy(showSipAddress = true) }
            is ContactCreationAction.HideSipAddress ->
                updateState { copy(showSipAddress = false, sipAddress = "") }
            is ContactCreationAction.SetPhoto -> updateState { copy(photoUri = action.uri) }
            is ContactCreationAction.RemovePhoto -> updateState { copy(photoUri = null) }
            is ContactCreationAction.RequestGallery ->
                viewModelScope.launch { _effects.send(ContactCreationEffect.LaunchGallery) }
            is ContactCreationAction.RequestCamera -> requestCamera()
            else -> handleFieldUpdateAction(action)
        }
    }

    /**
     * Handles all field-value update actions: name parts, organization, note, nickname, SIP,
     * groups, and repeatable-field CRUD (phone, email, address, event, relation, IM, website).
     */
    private fun handleFieldUpdateAction(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.UpdatePrefix -> updateName { copy(prefix = action.value) }
            is ContactCreationAction.UpdateFirstName -> updateName { copy(first = action.value) }
            is ContactCreationAction.UpdateMiddleName -> updateName { copy(middle = action.value) }
            is ContactCreationAction.UpdateLastName -> updateName { copy(last = action.value) }
            is ContactCreationAction.UpdateSuffix -> updateName { copy(suffix = action.value) }
            is ContactCreationAction.UpdateCompany ->
                updateState { copy(organization = organization.copy(company = action.value)) }
            is ContactCreationAction.UpdateJobTitle ->
                updateState { copy(organization = organization.copy(title = action.value)) }
            is ContactCreationAction.UpdateNote -> updateState { copy(note = action.value) }
            is ContactCreationAction.UpdateNickname -> updateState { copy(nickname = action.value) }
            is ContactCreationAction.UpdateSipAddress ->
                updateState { copy(sipAddress = action.value) }
            is ContactCreationAction.ToggleGroup ->
                updateState {
                    val existing = groups.find { it.groupId == action.groupId }
                    if (existing != null) {
                        copy(groups = groups.filterNot { it.groupId == action.groupId })
                    } else {
                        copy(
                            groups = groups +
                                GroupFieldState(groupId = action.groupId, title = action.title),
                        )
                    }
                }
            else -> handleContactInfoCrud(action)
        }
    }

    private fun handleContactInfoCrud(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.AddPhone ->
                updateState { copy(phoneNumbers = phoneNumbers + PhoneFieldState()) }
            is ContactCreationAction.RemovePhone ->
                updateState { copy(phoneNumbers = phoneNumbers.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdatePhone ->
                updateState {
                    copy(
                        phoneNumbers = phoneNumbers.map {
                            if (it.id == action.id) it.copy(number = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdatePhoneType ->
                updateState {
                    copy(
                        phoneNumbers = phoneNumbers.map {
                            if (it.id == action.id) it.copy(type = action.type) else it
                        },
                    )
                }
            is ContactCreationAction.AddEmail ->
                updateState { copy(emails = emails + EmailFieldState()) }
            is ContactCreationAction.RemoveEmail ->
                updateState { copy(emails = emails.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdateEmail ->
                updateState {
                    copy(
                        emails = emails.map {
                            if (it.id == action.id) it.copy(address = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdateEmailType ->
                updateState {
                    copy(
                        emails = emails.map {
                            if (it.id == action.id) it.copy(type = action.type) else it
                        },
                    )
                }
            else -> handleAddressCrud(action)
        }
    }

    private fun handleAddressCrud(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.AddAddress ->
                updateState { copy(addresses = addresses + AddressFieldState()) }
            is ContactCreationAction.RemoveAddress ->
                updateState {
                    copy(addresses = addresses.filterNot { it.id == action.id })
                }
            is ContactCreationAction.UpdateAddressStreet,
            is ContactCreationAction.UpdateAddressCity,
            is ContactCreationAction.UpdateAddressRegion,
            is ContactCreationAction.UpdateAddressPostcode,
            is ContactCreationAction.UpdateAddressCountry,
            is ContactCreationAction.UpdateAddressType,
            -> handleAddressFieldUpdate(action)
            else -> handleMoreFieldsCrud(action)
        }
    }

    private fun handleAddressFieldUpdate(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.UpdateAddressStreet ->
                updateAddress(action.id) { copy(street = action.value) }
            is ContactCreationAction.UpdateAddressCity ->
                updateAddress(action.id) { copy(city = action.value) }
            is ContactCreationAction.UpdateAddressRegion ->
                updateAddress(action.id) { copy(region = action.value) }
            is ContactCreationAction.UpdateAddressPostcode ->
                updateAddress(action.id) { copy(postcode = action.value) }
            is ContactCreationAction.UpdateAddressCountry ->
                updateAddress(action.id) { copy(country = action.value) }
            is ContactCreationAction.UpdateAddressType ->
                updateAddress(action.id) { copy(type = action.type) }
            else -> Unit
        }
    }

    private inline fun updateAddress(
        id: String,
        crossinline transform: AddressFieldState.() -> AddressFieldState,
    ) {
        updateState {
            copy(addresses = addresses.map { if (it.id == id) it.transform() else it })
        }
    }

    private fun handleMoreFieldsCrud(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.AddEvent ->
                updateState { copy(events = events + EventFieldState()) }
            is ContactCreationAction.RemoveEvent ->
                updateState { copy(events = events.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdateEvent ->
                updateState {
                    copy(
                        events = events.map {
                            if (it.id == action.id) it.copy(startDate = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdateEventType ->
                updateState {
                    copy(
                        events = events.map {
                            if (it.id == action.id) it.copy(type = action.type) else it
                        },
                    )
                }
            is ContactCreationAction.AddRelation ->
                updateState { copy(relations = relations + RelationFieldState()) }
            is ContactCreationAction.RemoveRelation ->
                updateState { copy(relations = relations.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdateRelation ->
                updateState {
                    copy(
                        relations = relations.map {
                            if (it.id == action.id) it.copy(name = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdateRelationType ->
                updateState {
                    copy(
                        relations = relations.map {
                            if (it.id == action.id) it.copy(type = action.type) else it
                        },
                    )
                }
            else -> handleImWebsiteCrud(action)
        }
    }

    private fun handleImWebsiteCrud(action: ContactCreationAction) {
        when (action) {
            is ContactCreationAction.AddIm ->
                updateState { copy(imAccounts = imAccounts + ImFieldState()) }
            is ContactCreationAction.RemoveIm ->
                updateState { copy(imAccounts = imAccounts.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdateIm ->
                updateState {
                    copy(
                        imAccounts = imAccounts.map {
                            if (it.id == action.id) it.copy(data = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdateImProtocol ->
                updateState {
                    copy(
                        imAccounts = imAccounts.map {
                            if (it.id == action.id) it.copy(protocol = action.protocol) else it
                        },
                    )
                }
            is ContactCreationAction.AddWebsite ->
                updateState { copy(websites = websites + WebsiteFieldState()) }
            is ContactCreationAction.RemoveWebsite ->
                updateState { copy(websites = websites.filterNot { it.id == action.id }) }
            is ContactCreationAction.UpdateWebsite ->
                updateState {
                    copy(
                        websites = websites.map {
                            if (it.id == action.id) it.copy(url = action.value) else it
                        },
                    )
                }
            is ContactCreationAction.UpdateWebsiteType ->
                updateState {
                    copy(
                        websites = websites.map {
                            if (it.id == action.id) it.copy(type = action.type) else it
                        },
                    )
                }
            else -> Unit
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
        if (state.isSaving) return
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
                updateState { copy(showDiscardDialog = true) }
            } else {
                _effects.send(ContactCreationEffect.NavigateBack)
            }
        }
    }

    private fun confirmDiscard() {
        viewModelScope.launch {
            updateState { copy(showDiscardDialog = false) }
            _effects.send(ContactCreationEffect.NavigateBack)
        }
    }

    private fun dismissDiscardDialog() {
        updateState { copy(showDiscardDialog = false) }
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

    /** URI of the file passed to ACTION_IMAGE_CAPTURE, persisted across process death. */
    internal var pendingCameraUri: Uri?
        get() = savedStateHandle.get<Uri>(PENDING_CAMERA_URI_KEY)
        set(value) {
            savedStateHandle[PENDING_CAMERA_URI_KEY] = value
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
        const val SAVE_MODE_EXTRA_KEY = "saveMode"
    }
}

private const val PENDING_CAMERA_URI_KEY = "pendingCameraUri"
private const val PHOTO_CACHE_DIR = "contact_photos"
private const val ACCOUNT_LOAD_TIMEOUT_SECONDS = 5L
