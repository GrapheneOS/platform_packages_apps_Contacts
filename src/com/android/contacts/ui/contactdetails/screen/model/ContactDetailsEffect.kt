package com.android.contacts.ui.contactdetails.screen.model

import android.net.Uri
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.domain.contactdetails.model.ContactEntryAction

internal sealed interface ContactDetailsEffect {

    data object ViewCallLog : ContactDetailsEffect

    data class AddDirectoryContact(
        val prefill: DirectoryContactPrefill,
    ) : ContactDetailsEffect

    data class EditContact(
        val lookupUri: Uri,
        val photoId: Long,
    ) : ContactDetailsEffect

    data class ConfirmDelete(
        val lookupUri: Uri,
    ) : ContactDetailsEffect

    data class ShareContact(
        val lookupKey: String,
    ) : ContactDetailsEffect

    data class PickRingtone(
        val currentRingtone: String?,
    ) : ContactDetailsEffect

    data class PickJoinTarget(
        val contactId: Long,
    ) : ContactDetailsEffect

    data class ViewGroupMembers(
        val groupId: Long,
    ) : ContactDetailsEffect

    data class ViewLinkedContacts(
        val lookupUri: Uri,
    ) : ContactDetailsEffect

    data class PerformEntryAction(
        val action: ContactEntryAction,
    ) : ContactDetailsEffect

    data class CallWithNote(
        val number: String,
        val displayNumber: String?,
        val numberLabel: String?,
        val lookupUri: Uri?,
        val displayName: String?,
        val photoId: Long,
        val photoUri: String?,
    ) : ContactDetailsEffect

    data class CopyToClipboard(
        val label: String?,
        val text: String,
    ) : ContactDetailsEffect
}
