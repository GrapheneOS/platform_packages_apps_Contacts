package com.android.contacts.ui.interactions.callsubject.screen.model

import android.telecom.PhoneAccountHandle

internal sealed interface CallSubjectEffect {

    data object Close : CallSubjectEffect

    data class PlaceCall(
        val subject: String,
        val number: String,
        val phoneAccountHandle: PhoneAccountHandle?,
    ) : CallSubjectEffect
}
