package com.android.contacts.ui.contactdetails.screen.model

internal sealed interface ContactDetailsNavEvent {
    data object Close : ContactDetailsNavEvent
}
