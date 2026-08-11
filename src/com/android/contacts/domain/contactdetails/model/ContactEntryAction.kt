package com.android.contacts.domain.contactdetails.model

internal sealed interface ContactEntryAction {

    data class Call(
        val number: String,
    ) : ContactEntryAction

    data class Sms(
        val number: String,
    ) : ContactEntryAction

    data class VideoCall(
        val number: String,
    ) : ContactEntryAction

    data class CallWithNote(
        val number: String,
        val formattedNumber: String?,
        val numberLabel: String?,
    ) : ContactEntryAction

    data class SipCall(
        val address: String,
    ) : ContactEntryAction

    data class SendEmail(
        val address: String,
    ) : ContactEntryAction

    data class ShowOnMap(
        val address: String,
    ) : ContactEntryAction

    data class ShowDirections(
        val address: String,
    ) : ContactEntryAction

    data class OpenUrl(
        val url: String,
    ) : ContactEntryAction

    data class OpenChat(
        val data: String,
        val protocol: Int,
        val customProtocol: String?,
    ) : ContactEntryAction

    data class ShowEventDate(
        val date: String,
        val isRecurringAnnually: Boolean,
    ) : ContactEntryAction

    data class SearchContacts(
        val query: String,
    ) : ContactEntryAction

    data class ViewDataItem(
        val dataId: Long,
        val mimeType: String,
    ) : ContactEntryAction
}
