package com.android.contacts.ui.contactdetails.screen.delegate

import android.net.Uri
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import com.android.contacts.ui.contactdetails.screen.model.PendingContactFlags
import com.android.contacts.ui.contactdetails.screen.model.PendingContactRingtone
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface ContactFlagsDelegate {
    val pendingFlags: StateFlow<PendingContactFlags>

    fun bind(
        scope: CoroutineScope,
        details: StateFlow<ContactDetails?>,
    )

    fun clearApplied(details: ContactDetails)

    fun toggleStarred(lookupUri: Uri)

    fun toggleSendToVoicemail(lookupUri: Uri)

    fun setRingtone(
        lookupUri: Uri,
        ringtone: String?,
    )
}

internal class ContactFlagsDelegateImpl @Inject constructor(
    private val contactActionsRepository: ContactActionsRepository,
) : ContactFlagsDelegate {

    private val _pendingFlags = MutableStateFlow(PendingContactFlags())
    override val pendingFlags: StateFlow<PendingContactFlags> = _pendingFlags.asStateFlow()

    private var loadedDetails: StateFlow<ContactDetails?>? = null
    private var boundScope: CoroutineScope? = null

    override fun bind(
        scope: CoroutineScope,
        details: StateFlow<ContactDetails?>,
    ) {
        if (boundScope != null) {
            return
        }

        boundScope = scope
        loadedDetails = details
    }

    override fun clearApplied(details: ContactDetails) {
        _pendingFlags.update { pending -> pending.withoutApplied(details) }
    }

    override fun toggleStarred(lookupUri: Uri) {
        val details = effectiveDetails() ?: return

        toggle(
            current = details.isStarred,
            updatePending = { pending ->
                _pendingFlags.update { it.copy(isStarred = pending) }
            },
            write = { isEnabled ->
                contactActionsRepository.setStarred(lookupUri, isEnabled)
            },
        )
    }

    override fun toggleSendToVoicemail(lookupUri: Uri) {
        val details = effectiveDetails() ?: return

        toggle(
            current = details.isSendToVoicemail,
            updatePending = { pending ->
                _pendingFlags.update { it.copy(isSendToVoicemail = pending) }
            },
            write = { isEnabled ->
                contactActionsRepository.setSendToVoicemail(lookupUri, isEnabled)
            },
        )
    }

    override fun setRingtone(
        lookupUri: Uri,
        ringtone: String?,
    ) {
        updatePendingRingtone(PendingContactRingtone(uri = ringtone))

        boundScope?.launch {
            try {
                contactActionsRepository.setRingtone(lookupUri, ringtone)
            } catch (_: IllegalStateException) {
                updatePendingRingtone(null)
            } catch (_: SecurityException) {
                updatePendingRingtone(null)
            }
        }
    }

    private fun updatePendingRingtone(pending: PendingContactRingtone?) {
        _pendingFlags.update { flags -> flags.copy(ringtone = pending) }
    }

    private fun effectiveDetails(): ContactDetails? {
        val details = loadedDetails?.value ?: return null

        return _pendingFlags.value.applyTo(details)
    }

    private fun toggle(
        current: Boolean,
        updatePending: (Boolean?) -> Unit,
        write: suspend (Boolean) -> Unit,
    ) {
        val isEnabled = !current

        updatePending(isEnabled)

        boundScope?.launch {
            try {
                write(isEnabled)
            } catch (_: IllegalStateException) {
                updatePending(null)
            } catch (_: SecurityException) {
                updatePending(null)
            }
        }
    }
}
