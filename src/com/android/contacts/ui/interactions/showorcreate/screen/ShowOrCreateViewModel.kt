package com.android.contacts.ui.interactions.showorcreate.screen

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.PhoneAccount
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.ContactsUtils
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateAction as Action
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateEffect as Effect
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface ShowOrCreateScreenModel {
    val effects: Flow<Effect>
    val uiState: StateFlow<State>

    fun onAction(action: Action)
}

@HiltViewModel
internal class ShowOrCreateViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactsRepository: ContactsRepository,
) : ViewModel(),
    ShowOrCreateScreenModel {

    private val _effects = Channel<Effect>(capacity = Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<State>(State.Searching)
    override val uiState = _uiState.asStateFlow()

    private val data: Uri?
        get() = savedStateHandle[EXTRA_DATA] as? Uri
    private val scheme: String?
        get() = data?.scheme
    private val schemeSpecificPart: String?
        get() = data?.schemeSpecificPart
    private val createDescription: String?
        get() = savedStateHandle[ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION]
            ?: schemeSpecificPart
    private val forceCreate: Boolean?
        get() = savedStateHandle[ContactsContract.Intents.EXTRA_FORCE_CREATE] as? Boolean
    private val originalExtras: Bundle?
        get() = savedStateHandle[EXTRA_EXTRAS] as? Bundle

    private var query: ContactLookupQuery? = null

    init {
        query = when (scheme) {
            ContactsUtils.SCHEME_MAILTO -> {
                ContactLookupQuery.Email(schemeSpecificPart.orEmpty())
            }
            PhoneAccount.SCHEME_TEL -> {
                ContactLookupQuery.Phone(schemeSpecificPart.orEmpty())
            }
            else -> {
                Log.w(TAG, "Invalid intent scheme: $scheme")
                emitEffect(Effect.Close)
                null
            }
        }
        query?.let(::lookupContacts)
    }

    override fun onAction(action: Action) {
        when (action) {
            Action.CreateConfirm -> {
                val query = query ?: return
                emitEffect(Effect.CreateContact(buildCreateExtras(query)))
            }
            Action.CreateDismiss -> {
                emitEffect(Effect.Close)
            }
        }
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private fun lookupContacts(query: ContactLookupQuery) {
        viewModelScope.launch {
            val results = contactsRepository.lookup(query).first()
            when {
                results.size == 1 -> {
                    val result = results.first()
                    emitEffect(Effect.ShowContact(result.uri))
                }
                results.size > 1 -> {
                    emitEffect(Effect.ShowContactList(buildCreateExtras(query)))
                }
                forceCreate == true -> {
                    emitEffect(Effect.CreateContact(buildCreateExtras(query)))
                }
                else -> {
                    _uiState.value = State.ConfirmingCreate(createDescription)
                }
            }
        }
    }

    private fun buildCreateExtras(query: ContactLookupQuery): Bundle {
        val extras = originalExtras?.deepCopy() ?: Bundle()
        when (query) {
            is ContactLookupQuery.Email -> {
                extras.putString(ContactsContract.Intents.Insert.EMAIL, query.value)
            }
            is ContactLookupQuery.Phone -> {
                extras.putString(ContactsContract.Intents.Insert.PHONE, query.value)
            }
        }
        return extras
    }

    companion object {
        private const val TAG = "ShowOrCreateViewModel"
        const val EXTRA_DATA = "data"
        const val EXTRA_EXTRAS = "extras"
    }
}
