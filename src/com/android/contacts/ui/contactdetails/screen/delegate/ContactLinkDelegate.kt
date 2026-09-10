package com.android.contacts.ui.contactdetails.screen.delegate

import android.app.Activity
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface ContactLinkDelegate {
    val progress: StateFlow<ContactLinkOperation?>
    val unlinked: Flow<Unit>

    fun bind(scope: CoroutineScope)

    fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    )
}

internal class ContactLinkDelegateImpl @Inject constructor(
    private val contactActionsRepository: ContactActionsRepository,
) : ContactLinkDelegate {

    private val _progress = MutableStateFlow(contactActionsRepository.getPendingLinkOperation())
    override val progress: StateFlow<ContactLinkOperation?> = _progress.asStateFlow()

    private val _unlinked = Channel<Unit>(Channel.BUFFERED)
    override val unlinked: Flow<Unit> = _unlinked.receiveAsFlow()

    private var boundScope: CoroutineScope? = null

    override fun bind(scope: CoroutineScope) {
        if (boundScope != null) {
            return
        }

        boundScope = scope

        contactActionsRepository.observeLinkOperations()
            .onEach { operation -> onLinkOperation(operation) }
            .launchIn(scope)
    }

    override fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    ) {
        _progress.value = ContactLinkOperation.LINK

        boundScope?.launch {
            contactActionsRepository.joinContacts(
                contactId = contactId,
                otherContactId = otherContactId,
                callbackActivity = callbackActivity,
            )
        }
    }

    private fun onLinkOperation(operation: ContactLinkOperation) {
        _progress.value = null

        if (operation == ContactLinkOperation.UNLINK) {
            _unlinked.trySend(Unit)
        }
    }
}
