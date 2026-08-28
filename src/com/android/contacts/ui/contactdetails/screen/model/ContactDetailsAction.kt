package com.android.contacts.ui.contactdetails.screen.model

import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.domain.contactdetails.model.ContactEntryAction

internal sealed interface ContactDetailsAction {

    sealed interface Menu : ContactDetailsAction

    sealed interface Entry : ContactDetailsAction

    sealed interface PickerResult : ContactDetailsAction

    data object BackClick : Menu
    data object StarClick : Menu
    data object EditClick : Menu
    data object DeleteClick : Menu
    data object ShareClick : Menu
    data object ShortcutClick : Menu
    data object RecentCallClick : Menu
    data object CallingSimClick : Menu
    data object RingtoneClick : Menu
    data object SendToVoicemailClick : Menu
    data object JoinClick : Menu
    data object LinkedContactsClick : Menu
    data object AddDetailsClick : Menu

    data class EntryClick(
        val action: ContactEntryAction,
    ) : Entry

    data class CopyClick(
        val label: String?,
        val text: String,
    ) : Entry

    data class SetDefaultClick(
        val dataId: Long,
    ) : Entry

    data class ClearDefaultClick(
        val dataId: Long,
    ) : Entry

    data object CallingSimDismissed : PickerResult

    data class CallingSimPicked(
        val selections: Map<Long, PhoneAccountId?>,
    ) : PickerResult

    data class RingtonePicked(
        val ringtone: String?,
    ) : PickerResult

    data class JoinTargetPicked(
        val contactId: Long,
    ) : PickerResult
}
