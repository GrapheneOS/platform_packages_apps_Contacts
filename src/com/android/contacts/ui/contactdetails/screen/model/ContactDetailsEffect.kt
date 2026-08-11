package com.android.contacts.ui.contactdetails.screen.model

import com.android.contacts.domain.contactdetails.model.ContactEntryAction

internal sealed interface ContactDetailsEffect {

    data object OpenEditor : ContactDetailsEffect
    data object ShareContact : ContactDetailsEffect
    data object ConfirmDelete : ContactDetailsEffect
    data object CreateShortcut : ContactDetailsEffect
    data object PickRingtone : ContactDetailsEffect
    data object PickJoinTarget : ContactDetailsEffect
    data object ViewLinkedContacts : ContactDetailsEffect

    data class PerformEntryAction(
        val action: ContactEntryAction,
    ) : ContactDetailsEffect

    data class CopyToClipboard(
        val label: String?,
        val text: String,
    ) : ContactDetailsEffect

    data class JoinContacts(
        val contactId: Long,
    ) : ContactDetailsEffect
}
