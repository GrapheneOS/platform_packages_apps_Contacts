package com.android.contacts.ui.editor.springboard.screen

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.domain.contacts.model.RawContactsResult
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.logging.EditorEvent
import com.android.contacts.logging.Logger
import com.android.contacts.ui.editor.springboard.ContactEditorSpringBoardActivity.Companion.EXTRA_SHOW_READ_ONLY
import com.android.contacts.ui.editor.springboard.ContactEditorSpringBoardActivity.Companion.EXTRA_URI
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapper
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardAction as Action
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardEffect as Effect
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardUiState as State
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import com.android.contacts.util.core.GetUriType
import com.google.i18n.phonenumbers.NumberParseException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface ContactEditorSpringBoardScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ContactEditorSpringBoardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUriType: GetUriType,
    private val loadRawContacts: LoadRawContacts,
    private val rawContactUiModelMapper: RawContactUiModelMapper,
) : ViewModel(),
    ContactEditorSpringBoardScreenModel {

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<State>(State.Loading)
    override val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val uri: Uri? = savedStateHandle[EXTRA_URI] as? Uri
    private val showReadOnly: Boolean = savedStateHandle.get<Boolean>(EXTRA_SHOW_READ_ONLY) == true

    private var result: RawContactsResult?
        get() = savedStateHandle[KEY_RESULT]
        set(value) {
            savedStateHandle[KEY_RESULT] = value
        }

    init {
        if (uri != null) {
            viewModelScope.launch {
                if (!handleRawContactUri(uri)) {
                    handleContactOrProfileUri(uri)
                }
            }
        } else {
            emitEffect(Effect.ShowErrorAndClose)
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CloseClicked -> emitEffect(Effect.Close)
            Action.AddClicked -> onAddClicked()
            is Action.ContactClicked -> onConctactClicked(action)
            Action.UnlinkClicked -> onUnlinkClicked()
            Action.UnlinkConfirmed -> onUnlinkConfirmed()
            Action.UnlinkDismissed -> emitEffect(Effect.Close)
            is Action.AddContactSelected -> onAddContactSelected(action.contactUri)
            Action.DialogDismissed -> emitEffect(Effect.Close)
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private suspend fun handleRawContactUri(uri: Uri): Boolean {
        val authority = uri.authority
        val type = getUriType(uri)

        return when {
            ContactsContract.AUTHORITY == authority &&
                ContactsContract.RawContacts.CONTENT_ITEM_TYPE == type
            -> {
                // Go straight to editor if we're passed a raw contact Uri
                when (val rawContactId = parseContentUriId(uri)) {
                    null -> {
                        emitEffect(Effect.ShowErrorAndClose)
                    }
                    else -> {
                        logPickerEditorEvent()
                        emitEffect(Effect.EditContact(uri, rawContactId))
                    }
                }
                true
            }
            @Suppress("DEPRECATION")
            android.provider.Contacts.AUTHORITY == authority -> {
                // Fail if given a legacy URI
                Log.e(
                    TAG,
                    "Legacy Uri was passed to editor.",
                    IllegalArgumentException("Legacy Uri was passed to editor"),
                )
                emitEffect(Effect.ShowErrorAndClose)
                true
            }
            else -> {
                false
            }
        }
    }

    private fun parseContentUriId(uri: Uri): Long? {
        return try {
            ContentUris.parseId(uri).takeIf { it != -1L }
        } catch (e: NumberParseException) {
            Log.w(TAG, "Could not parse ContentUri ID", e)
            null
        }
    }

    private suspend fun handleContactOrProfileUri(uri: Uri) {
        val result = this.result
            ?: loadRawContacts(
                contactUri = uri,
                onlyWritable = !showReadOnly,
            ).first()
        this.result = result

        if (result == null) {
            emitEffect(Effect.ShowErrorAndClose)
            return
        }

        val rawContactWithWritableAccount =
            result.rawContacts.firstOrNull { it.account.areContactsWritable }

        if (showReadOnly ||
            (result.rawContacts.size > 1 && rawContactWithWritableAccount != null)
        ) {
            showRawContactsDialog(result)
        } else if (rawContactWithWritableAccount != null) {
            logPickerEditorEvent()
            emitEffect(Effect.EditContact(uri, rawContactWithWritableAccount.id))
        } else {
            // If the contact has only read-only raw contacts, we'll want to let the editor create
            // the writable raw contact for it.
            logPickerEditorEvent()
            emitEffect(Effect.CreateContact(uri))
        }
    }

    private fun showRawContactsDialog(result: RawContactsResult) {
        val contacts = buildRawContactsUiModels(result)
        _uiState.value = when {
            showReadOnly -> State.ShowLinkedContacts(
                contacts = contacts,
                isUserProfile = result.isUserProfile,
            )
            else -> State.ShowPickContactToEdit(
                contacts = contacts,
            )
        }
        logPickerEditorEvent(contacts.size)
    }

    private fun buildRawContactsUiModels(
        result: RawContactsResult,
    ): ImmutableList<RawContactUiModel> {
        return result.rawContacts
            .map { rawContactUiModelMapper.map(it, result.isUserProfile) }
            .toImmutableList()
    }

    private fun logPickerEditorEvent(numberRawContacts: Int = 0) {
        Logger.logEditorEvent(EditorEvent.EventType.SHOW_RAW_CONTACT_PICKER, numberRawContacts)
    }

    private fun onAddClicked() {
        val contactId = result?.contactId ?: return
        emitEffect(Effect.SelectContactToJoin(contactId))
    }

    private fun onConctactClicked(action: Action.ContactClicked) {
        val uri = uri ?: return
        emitEffect(Effect.EditContact(uri, action.rawContactId))
    }

    private fun onUnlinkClicked() {
        _uiState.value = State.ShowUnlinkConfirmation
    }

    private fun onUnlinkConfirmed() {
        val rawContactIds = result?.rawContacts?.map { it.id }
        if (rawContactIds.isNullOrEmpty()) return
        emitEffect(Effect.UnlinkRawContacts(rawContactIds))
    }

    private fun onAddContactSelected(contactUri: Uri) {
        val contactId1 = result?.contactId ?: return
        val contactId2 = parseContentUriId(contactUri) ?: return
        emitEffect(Effect.JoinContacts(contactId1, contactId2))
    }

    companion object {
        private const val TAG = "ContactEditorSpringBoardViewModel"

        @VisibleForTesting
        const val KEY_RESULT = "result"
    }
}
