package com.android.contacts.data.contactdetails.repository

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.ContactsContract.Data
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.telecom.model.PhoneAccountId
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

    suspend fun setSendToVoicemail(
        lookupUri: Uri,
        isEnabled: Boolean,
    )

    suspend fun setSuperPrimary(dataId: Long)

    suspend fun clearPrimary(dataId: Long)

    suspend fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    )

    suspend fun setPreferredPhoneAccount(
        dataId: Long,
        account: PhoneAccountId?,
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
        val intent = ContactSaveService.createSetStarredIntent(context, lookupUri, isStarred)
        startService(intent)
    }

    override suspend fun setRingtone(
        lookupUri: Uri,
        ringtone: String?,
    ) {
        val intent = ContactSaveService.createSetRingtone(context, lookupUri, ringtone)
        startService(intent)
    }

    override suspend fun setSendToVoicemail(
        lookupUri: Uri,
        isEnabled: Boolean,
    ) {
        val intent = ContactSaveService.createSetSendToVoicemail(context, lookupUri, isEnabled)
        startService(intent)
    }

    override suspend fun setSuperPrimary(dataId: Long) {
        val intent = ContactSaveService.createSetSuperPrimaryIntent(context, dataId)
        startService(intent)
    }

    override suspend fun clearPrimary(dataId: Long) {
        val intent = ContactSaveService.createClearPrimaryIntent(context, dataId)
        startService(intent)
    }

    override suspend fun joinContacts(
        contactId: Long,
        otherContactId: Long,
        callbackActivity: Class<out Activity>,
    ) {
        val intent = ContactSaveService.createJoinContactsIntent(
            context,
            contactId,
            otherContactId,
            callbackActivity,
            Intent.ACTION_VIEW,
        )

        startService(intent)
    }

    override suspend fun setPreferredPhoneAccount(
        dataId: Long,
        account: PhoneAccountId?,
    ) {
        val values = ContentValues().apply {
            put(Data.PREFERRED_PHONE_ACCOUNT_COMPONENT_NAME, account?.componentName)
            put(Data.PREFERRED_PHONE_ACCOUNT_ID, account?.id)
        }

        withContext(ioDispatcher) {
            context.contentResolver.update(
                ContentUris.withAppendedId(Data.CONTENT_URI, dataId),
                values,
                null,
                null,
            )
        }
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
