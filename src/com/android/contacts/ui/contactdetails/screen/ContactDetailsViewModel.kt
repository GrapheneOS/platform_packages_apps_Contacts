package com.android.contacts.ui.contactdetails.screen

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.contactdetails.screen.delegate.ContactDetailsContentDelegate
import com.android.contacts.ui.contactdetails.screen.delegate.ContactFlagsDelegate
import com.android.contacts.ui.contactdetails.screen.delegate.ContactLinkDelegate
import com.android.contacts.ui.contactdetails.screen.model.CallingSimSelection
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsArguments
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent as NavEvent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal interface ContactDetailsScreenModel {
    val uiState: StateFlow<State>
    val effects: Flow<Effect>
    val navigationEvents: Flow<NavEvent>

    fun bind(
        arguments: ContactDetailsArguments,
        callbackActivity: Class<out Activity>,
    )

    fun onAction(action: Action)
}

@HiltViewModel
internal class ContactDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentDelegate: ContactDetailsContentDelegate,
    private val flagsDelegate: ContactFlagsDelegate,
    private val linkDelegate: ContactLinkDelegate,
    private val contactDetailsRepository: ContactDetailsRepository,
    private val contactActionsRepository: ContactActionsRepository,
    private val contactShortcutRepository: ContactShortcutRepository,
) : ViewModel(),
    ContactDetailsScreenModel {

    private val isShortcutUsageReported = AtomicBoolean(false)

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _navigationEvents = Channel<NavEvent>(Channel.BUFFERED)
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.receiveAsFlow()

    private val callingSimPickerVisible = MutableStateFlow(false)

    private val argumentsBundle: StateFlow<Bundle?> = savedStateHandle
        .getStateFlow(KEY_ARGUMENTS, null)

    private val arguments: Flow<ContactDetailsArguments> = argumentsBundle
        .mapNotNull { bundle -> bundle?.toArguments() }

    override val uiState: StateFlow<State> = combine(
        contentDelegate.content,
        linkDelegate.progress,
        callingSimPickerVisible,
    ) { loadedContent, pendingLinkOperation, isPickerVisible ->
        State(
            content = loadedContent,
            linkProgress = pendingLinkOperation,
            isCallingSimPickerVisible = isPickerVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MILLIS),
        initialValue = State(linkProgress = linkDelegate.progress.value),
    )

    init {
        flagsDelegate.bind(
            scope = viewModelScope,
            details = contentDelegate.loadedDetails,
        )

        linkDelegate.bind(
            scope = viewModelScope,
        )

        contentDelegate.bind(
            scope = viewModelScope,
            arguments = arguments,
        )

        linkDelegate.unlinked
            .onEach { sendNavigationEvent(NavEvent.Close) }
            .launchIn(viewModelScope)

        contentDelegate.loadedDetails
            .filterNotNull()
            .onEach { details -> reportShortcutUsed(details) }
            .launchIn(viewModelScope)
    }

    override fun bind(
        arguments: ContactDetailsArguments,
        callbackActivity: Class<out Activity>,
    ) {
        savedStateHandle[KEY_ARGUMENTS] = Bundle().apply {
            putParcelable(KEY_LOOKUP_URI, arguments.lookupUri)
            putStringArray(KEY_EXCLUDED_MIME_TYPES, arguments.excludedMimeTypes.toTypedArray())
            putString(KEY_PRIORITIZED_MIME_TYPE, arguments.prioritizedMimeType)
            putSerializable(KEY_CALLBACK_ACTIVITY, callbackActivity)
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            is Action.Menu -> onMenuAction(action)
            is Action.Entry -> onEntryAction(action)
            is Action.PickerResult -> onPickerResult(action)
        }
    }

    private fun onMenuAction(action: Action.Menu) {
        when (action) {
            is Action.BackClick -> sendNavigationEvent(NavEvent.Close)
            is Action.StarClick -> toggleStarred()
            is Action.SendToVoicemailClick -> toggleSendToVoicemail()
            is Action.EditClick -> editContact()
            is Action.AddDetailsClick -> editContact()
            is Action.DeleteClick -> deleteContact()
            is Action.ShareClick -> shareContact()
            is Action.ShortcutClick -> createShortcut()
            is Action.RecentCallClick -> sendEffect(Effect.ViewCallLog)
            is Action.CallingSimClick -> showCallingSimPicker()
            is Action.RingtoneClick -> pickRingtone()
            is Action.JoinClick -> pickJoinTarget()
            is Action.LinkedContactsClick -> viewLinkedContacts()
            is Action.GroupClick -> sendEffect(Effect.ViewGroupMembers(action.groupId))
        }
    }

    private fun onEntryAction(action: Action.Entry) {
        when (action) {
            is Action.SetDefaultClick -> setDefault(action.dataId)
            is Action.ClearDefaultClick -> clearDefault(action.dataId)
            is Action.EntryClick -> performEntryAction(action.action)
            is Action.CopyClick -> sendEffect(Effect.CopyToClipboard(action.label, action.text))
        }
    }

    private fun onPickerResult(action: Action.PickerResult) {
        when (action) {
            is Action.CallingSimDismissed -> callingSimPickerVisible.value = false
            is Action.CallingSimPicked -> setCallingSims(action.selections)
            is Action.RingtonePicked -> setRingtone(action.ringtone)
            is Action.JoinTargetPicked -> joinContact(action.contactId)
        }
    }

    private fun showCallingSimPicker() {
        callingSimPickerVisible.value = true
    }

    private fun setCallingSims(selections: List<CallingSimSelection>) {
        callingSimPickerVisible.value = false

        viewModelScope.launch {
            for (selection in selections) {
                contactActionsRepository.setPreferredPhoneAccount(
                    dataId = selection.dataId,
                    account = selection.accountId,
                )
            }
        }
    }

    private fun toggleSendToVoicemail() {
        val lookupUri = lookupUri() ?: return

        flagsDelegate.toggleSendToVoicemail(lookupUri)
    }

    private fun toggleStarred() {
        val lookupUri = lookupUri() ?: return

        flagsDelegate.toggleStarred(lookupUri)
    }

    private fun editContact() {
        val details = contentDelegate.loadedDetails.value ?: return
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
        val lookupUri = contentDelegate.loadedDetails.value?.lookupUri ?: return

        sendEffect(Effect.ConfirmDelete(lookupUri))
    }

    private fun shareContact() {
        val lookupKey = contentDelegate.loadedDetails.value?.lookupKey ?: return

        sendEffect(Effect.ShareContact(lookupKey))
    }

    private fun reportShortcutUsed(details: ContactDetails) {
        if (!isShortcutUsageReported.compareAndSet(false, true)) return

        contactShortcutRepository.reportShortcutUsed(details.lookupKey)
    }

    private fun createShortcut() {
        val details = contentDelegate.loadedDetails.value ?: return

        contactShortcutRepository.requestPinShortcut(
            contactId = details.contactId,
            lookupKey = details.lookupKey,
            displayName = details.displayName,
        )
    }

    private fun pickRingtone() {
        val details = contentDelegate.loadedDetails.value ?: return

        sendEffect(Effect.PickRingtone(details.customRingtone))
    }

    private fun pickJoinTarget() {
        val details = contentDelegate.loadedDetails.value ?: return

        sendEffect(Effect.PickJoinTarget(details.contactId))
    }

    private fun viewLinkedContacts() {
        val lookupUri = contentDelegate.loadedDetails.value?.lookupUri ?: return

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
        val details = contentDelegate.loadedDetails.value ?: return null

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
        val details = contentDelegate.loadedDetails.value ?: return
        val callbackActivity = callbackActivity() ?: return

        linkDelegate.joinContacts(
            contactId = details.contactId,
            otherContactId = targetContactId,
            callbackActivity = callbackActivity,
        )
    }

    private fun sendEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun sendNavigationEvent(event: NavEvent) {
        _navigationEvents.trySend(event)
    }

    private fun lookupUri(): Uri? {
        return currentArguments()?.lookupUri
    }

    private fun currentArguments(): ContactDetailsArguments? {
        return argumentsBundle.value?.toArguments()
    }

    private fun callbackActivity(): Class<out Activity>? {
        val bundle = argumentsBundle.value ?: return null

        @Suppress("UNCHECKED_CAST")
        return BundleCompat.getSerializable(
            bundle,
            KEY_CALLBACK_ACTIVITY,
            Class::class.java,
        ) as Class<out Activity>?
    }

    private fun Bundle.toArguments(): ContactDetailsArguments? {
        val lookupUri = BundleCompat.getParcelable(
            this,
            KEY_LOOKUP_URI,
            Uri::class.java,
        ) ?: return null

        return ContactDetailsArguments(
            lookupUri = lookupUri,
            excludedMimeTypes = getStringArray(KEY_EXCLUDED_MIME_TYPES).orEmpty().toSet(),
            prioritizedMimeType = getString(KEY_PRIORITIZED_MIME_TYPE),
        )
    }

    private companion object {
        const val STATE_TIMEOUT_MILLIS = 5_000L
        const val KEY_ARGUMENTS = "contact_details_arguments"
        const val KEY_LOOKUP_URI = "contact_details_lookup_uri"
        const val KEY_EXCLUDED_MIME_TYPES = "contact_details_excluded_mime_types"
        const val KEY_PRIORITIZED_MIME_TYPE = "contact_details_prioritized_mime_type"
        const val KEY_CALLBACK_ACTIVITY = "contact_details_callback_activity"
    }
}
