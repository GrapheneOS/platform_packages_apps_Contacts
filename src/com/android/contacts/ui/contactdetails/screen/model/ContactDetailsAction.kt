package com.android.contacts.ui.contactdetails.screen.model

import com.android.contacts.domain.contactdetails.model.ContactEntryAction

internal sealed interface ContactDetailsAction {

    data object BackClick : ContactDetailsAction
    data object StarClick : ContactDetailsAction
    data object EditClick : ContactDetailsAction
    data object DeleteClick : ContactDetailsAction
    data object ShareClick : ContactDetailsAction
    data object ShortcutClick : ContactDetailsAction
    data object RingtoneClick : ContactDetailsAction
    data object JoinClick : ContactDetailsAction
    data object LinkedContactsClick : ContactDetailsAction
    data object AddDetailsClick : ContactDetailsAction

    data class EntryClick(
        val action: ContactEntryAction,
    ) : ContactDetailsAction

    data class CopyClick(
        val label: String?,
        val text: String,
    ) : ContactDetailsAction

    data class SetDefaultClick(
        val dataId: Long,
    ) : ContactDetailsAction

    data class ClearDefaultClick(
        val dataId: Long,
    ) : ContactDetailsAction

    data class RingtonePicked(
        val ringtone: String?,
    ) : ContactDetailsAction

    data class JoinTargetPicked(
        val contactId: Long,
    ) : ContactDetailsAction
}
