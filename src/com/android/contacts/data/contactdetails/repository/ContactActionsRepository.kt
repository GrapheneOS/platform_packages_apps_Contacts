package com.android.contacts.data.contactdetails.repository

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.di.core.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

internal interface ContactActionsRepository {

    suspend fun setStarred(
        lookupUri: Uri,
        isStarred: Boolean,
    )

    suspend fun setRingtone(
        lookupUri: Uri,
        ringtone: String?,
    )

    suspend fun setSuperPrimary(dataId: Long)

    suspend fun clearPrimary(dataId: Long)

    suspend fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    )

    fun observeLinkOperations(): Flow<ContactLinkOperation>

    fun getPendingLinkOperation(): ContactLinkOperation?
}

internal class ContactActionsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localBroadcastManager: LocalBroadcastManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactActionsRepository {

    override suspend fun setStarred(
        lookupUri: Uri,
        isStarred: Boolean,
    ) {
        startService(ContactSaveService.createSetStarredIntent(context, lookupUri, isStarred))
    }

    override suspend fun setRingtone(
        lookupUri: Uri,
        ringtone: String?,
    ) {
        startService(ContactSaveService.createSetRingtone(context, lookupUri, ringtone))
    }

    override suspend fun setSuperPrimary(dataId: Long) {
        startService(ContactSaveService.createSetSuperPrimaryIntent(context, dataId))
    }

    override suspend fun clearPrimary(dataId: Long) {
        startService(ContactSaveService.createClearPrimaryIntent(context, dataId))
    }

    override suspend fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    ) {
        startService(
            ContactSaveService.createJoinContactsIntent(
                context,
                contactId,
                otherContactId,
                callbackActivity,
                Intent.ACTION_VIEW,
            ),
        )
    }

    override fun observeLinkOperations(): Flow<ContactLinkOperation> {
        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    val operation = toLinkOperation(intent.action) ?: return
                    trySend(operation)
                }
            }

            localBroadcastManager.registerReceiver(receiver, linkOperationFilter())

            awaitClose {
                localBroadcastManager.unregisterReceiver(receiver)
            }
        }
    }

    override fun getPendingLinkOperation(): ContactLinkOperation? {
        val state = ContactSaveService.getState()
        val isUnlinkPending = state.isActionPending(ContactSaveService.ACTION_SPLIT_CONTACT)
        val isLinkPending = state.isActionPending(ContactSaveService.ACTION_JOIN_CONTACTS)

        return when {
            isUnlinkPending -> ContactLinkOperation.UNLINK
            isLinkPending -> ContactLinkOperation.LINK
            else -> null
        }
    }

    private suspend fun startService(intent: Intent) {
        withContext(ioDispatcher) {
            context.startService(intent)
        }
    }

    private fun linkOperationFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(ContactSaveService.BROADCAST_LINK_COMPLETE)
            addAction(ContactSaveService.BROADCAST_UNLINK_COMPLETE)
        }
    }

    private fun toLinkOperation(action: String?): ContactLinkOperation? {
        return when (action) {
            ContactSaveService.BROADCAST_LINK_COMPLETE -> ContactLinkOperation.LINK
            ContactSaveService.BROADCAST_UNLINK_COMPLETE -> ContactLinkOperation.UNLINK
            else -> null
        }
    }
}
