@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.usecase.BuildContactDetailsCards
import com.android.contacts.domain.contactdetails.usecase.GetContactDetailsMenu
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapper
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent as NavEvent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal interface ContactDetailsScreenModel {
    val uiState: StateFlow<State>
    val effects: Flow<Effect>
    val navigationEvents: Flow<NavEvent>

    fun bind(
        lookupUri: Uri,
        excludedMimeTypes: Set<String>,
        prioritizedMimeType: String?,
        callbackActivity: Class<out Activity>,
    )

    fun onAction(action: Action)
}

@HiltViewModel
internal class ContactDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactDetailsRepository: ContactDetailsRepository,
    private val contactActionsRepository: ContactActionsRepository,
    private val buildContactDetailsCards: BuildContactDetailsCards,
    private val getContactDetailsMenu: GetContactDetailsMenu,
    private val contactShortcutRepository: ContactShortcutRepository,
    private val contactDetailsUiStateMapper: ContactDetailsUiStateMapper,
) : ViewModel(),
    ContactDetailsScreenModel {

    @Volatile
    private var loadedDetails: ContactDetails? = null

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _navigationEvents = Channel<NavEvent>(Channel.BUFFERED)
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.receiveAsFlow()

    private val _linkProgress = MutableStateFlow(contactActionsRepository.getPendingLinkOperation())

    private val content: Flow<Content> = savedStateHandle
        .getStateFlow<Bundle?>(KEY_ARGUMENTS, null)
        .filterNotNull()
        .flatMapLatest { arguments ->
            observeContent(arguments)
        }
        .onStart { emit(Content.Loading) }

    override val uiState: StateFlow<State> = combine(
        content,
        _linkProgress,
    ) { content, linkProgress ->
        State(content = content, linkProgress = linkProgress)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS),
        initialValue = State(linkProgress = _linkProgress.value),
    )

    init {
        contactActionsRepository.observeLinkOperations()
            .onEach { operation ->
                onLinkOperation(operation)
            }
            .launchIn(viewModelScope)
    }

    override fun bind(
        lookupUri: Uri,
        excludedMimeTypes: Set<String>,
        prioritizedMimeType: String?,
        callbackActivity: Class<out Activity>,
    ) {
        savedStateHandle[KEY_ARGUMENTS] = Bundle().apply {
            putParcelable(KEY_LOOKUP_URI, lookupUri)
            putStringArray(KEY_EXCLUDED_MIME_TYPES, excludedMimeTypes.toTypedArray())
            putString(KEY_PRIORITIZED_MIME_TYPE, prioritizedMimeType)
            putSerializable(KEY_CALLBACK_ACTIVITY, callbackActivity)
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            is Action.BackClick -> sendNavigationEvent(NavEvent.Close)
            is Action.StarClick -> toggleStarred()
            is Action.EditClick -> editContact()
            is Action.AddDetailsClick -> editContact()
            is Action.DeleteClick -> deleteContact()
            is Action.ShareClick -> shareContact()
            is Action.ShortcutClick -> createShortcut()
            is Action.RingtoneClick -> pickRingtone()
            is Action.JoinClick -> pickJoinTarget()
            is Action.LinkedContactsClick -> viewLinkedContacts()
            is Action.SetDefaultClick -> setDefault(action.dataId)
            is Action.ClearDefaultClick -> clearDefault(action.dataId)
            is Action.RingtonePicked -> setRingtone(action.ringtone)
            is Action.JoinTargetPicked -> joinContact(action.contactId)
            is Action.EntryClick -> performEntryAction(action.action)

            is Action.CopyClick -> {
                sendEffect(Effect.CopyToClipboard(action.label, action.text))
            }
        }
    }

    private fun observeContent(arguments: Bundle): Flow<Content> {
        val lookupUri = lookupUri(arguments) ?: return flowOf(Content.Loading)

        return contactDetailsRepository
            .observeContactDetails(lookupUri, excludedMimeTypes(arguments))
            .map { result -> toContent(result, arguments) }
    }

    private fun toContent(
        result: ContactDetailsResult,
        arguments: Bundle,
    ): Content {
        return when (result) {
            is ContactDetailsResult.NotFound -> Content.NotFound
            is ContactDetailsResult.Error -> Content.Error
            is ContactDetailsResult.Loaded -> toLoadedContent(result.details, arguments)
        }
    }

    private fun toLoadedContent(
        details: ContactDetails,
        arguments: Bundle,
    ): Content {
        loadedDetails = details

        return contactDetailsUiStateMapper.map(
            details = details,
            cards = buildContactDetailsCards(details, prioritizedMimeType(arguments)),
            menu = getContactDetailsMenu(details.capabilities),
        )
    }

    private fun onLinkOperation(operation: ContactLinkOperation) {
        _linkProgress.value = null

        if (operation == ContactLinkOperation.UNLINK) {
            sendNavigationEvent(NavEvent.Close)
        }
    }

    private fun toggleStarred() {
        val lookupUri = lookupUri() ?: return
        val loaded = uiState.value.content as? Content.Loaded ?: return

        viewModelScope.launch {
            contactActionsRepository.setStarred(lookupUri, !loaded.isStarred)
        }
    }

    private fun editContact() {
        val details = loadedDetails ?: return
        val lookupUri = details.lookupUri ?: return

        when {
            details.capabilities.isDirectoryEntry -> addDirectoryContact()
            details.capabilities.isInvisibleAndAddable -> addToDefaultGroup()

            else -> {
                contactDetailsRepository.cacheLoadedContact()
                sendEffect(Effect.EditContact(lookupUri, details.photoId))
            }
        }
    }

    private fun addDirectoryContact() {
        val prefill = contactDetailsRepository.getDirectoryContactPrefill() ?: return

        sendEffect(Effect.AddDirectoryContact(prefill))
    }

    private fun addToDefaultGroup() {
        val callbackActivity = callbackActivity() ?: return

        contactDetailsRepository.addLoadedContactToDefaultGroup(callbackActivity)
    }

    private fun deleteContact() {
        val lookupUri = loadedDetails?.lookupUri ?: return

        sendEffect(Effect.ConfirmDelete(lookupUri))
    }

    private fun shareContact() {
        val lookupKey = loadedDetails?.lookupKey ?: return

        sendEffect(Effect.ShareContact(lookupKey))
    }

    private fun createShortcut() {
        val details = loadedDetails ?: return
        val loaded = uiState.value.content as? Content.Loaded ?: return

        contactShortcutRepository.requestPinShortcut(
            contactId = details.contactId,
            lookupKey = details.lookupKey,
            displayName = loaded.header.displayName,
        )
    }

    private fun pickRingtone() {
        val details = loadedDetails ?: return

        sendEffect(Effect.PickRingtone(details.customRingtone))
    }

    private fun pickJoinTarget() {
        val details = loadedDetails ?: return

        sendEffect(Effect.PickJoinTarget(details.contactId))
    }

    private fun viewLinkedContacts() {
        val lookupUri = loadedDetails?.lookupUri ?: return

        sendEffect(Effect.ViewLinkedContacts(lookupUri))
    }

    private fun performEntryAction(action: ContactEntryAction) {
        val effect = when (action) {
            is ContactEntryAction.CallWithNote -> callWithNoteEffect(action) ?: return
            else -> Effect.PerformEntryAction(action)
        }

        sendEffect(effect)
    }

    private fun callWithNoteEffect(action: ContactEntryAction.CallWithNote): Effect? {
        val details = loadedDetails ?: return null

        return Effect.CallWithNote(
            number = action.number,
            displayNumber = action.formattedNumber,
            numberLabel = action.numberLabel,
            lookupUri = details.lookupUri,
            displayName = details.displayName,
            photoId = details.photoId,
            photoUri = (details.photo as? ContactPhoto.Uri)?.value,
        )
    }

    private fun setDefault(dataId: Long) {
        viewModelScope.launch {
            contactActionsRepository.setSuperPrimary(dataId)
        }
    }

    private fun clearDefault(dataId: Long) {
        viewModelScope.launch {
            contactActionsRepository.clearPrimary(dataId)
        }
    }

    private fun setRingtone(ringtone: String?) {
        val lookupUri = lookupUri() ?: return

        viewModelScope.launch {
            contactActionsRepository.setRingtone(lookupUri, ringtone)
        }
    }

    private fun joinContact(targetContactId: Long) {
        val details = loadedDetails ?: return
        val callbackActivity = callbackActivity() ?: return

        _linkProgress.value = ContactLinkOperation.LINK

        viewModelScope.launch {
            contactActionsRepository.joinContacts(
                contactId = details.contactId,
                otherContactId = targetContactId,
                callbackActivity = callbackActivity,
            )
        }
    }

    private fun sendEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun sendNavigationEvent(event: NavEvent) {
        _navigationEvents.trySend(event)
    }

    private fun lookupUri(): Uri? {
        return arguments()?.let { arguments -> lookupUri(arguments) }
    }

    private fun callbackActivity(): Class<out Activity>? {
        val arguments = arguments() ?: return null

        @Suppress("UNCHECKED_CAST")
        return BundleCompat.getSerializable(
            arguments,
            KEY_CALLBACK_ACTIVITY,
            Class::class.java,
        ) as Class<out Activity>?
    }

    private fun arguments(): Bundle? {
        return savedStateHandle[KEY_ARGUMENTS]
    }

    private fun lookupUri(arguments: Bundle): Uri? {
        return BundleCompat.getParcelable(arguments, KEY_LOOKUP_URI, Uri::class.java)
    }

    private fun excludedMimeTypes(arguments: Bundle): Set<String> {
        return arguments.getStringArray(KEY_EXCLUDED_MIME_TYPES)
            .orEmpty()
            .toSet()
    }

    private fun prioritizedMimeType(arguments: Bundle): String? {
        return arguments.getString(KEY_PRIORITIZED_MIME_TYPE)
    }

    private companion object {
        const val KEY_ARGUMENTS = "contact_details_arguments"
        const val KEY_LOOKUP_URI = "contact_details_lookup_uri"
        const val KEY_EXCLUDED_MIME_TYPES = "contact_details_excluded_mime_types"
        const val KEY_PRIORITIZED_MIME_TYPE = "contact_details_prioritized_mime_type"
        const val KEY_CALLBACK_ACTIVITY = "contact_details_callback_activity"
        const val STATE_TIMEOUT_MILLIS = 5_000L
    }
}
