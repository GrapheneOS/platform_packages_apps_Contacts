package com.android.contacts.ui.contactdetails.screen.model

import com.android.contacts.data.contactdetails.model.ContactDetails

internal data class PendingContactFlags(
    val isStarred: Boolean? = null,
    val isSendToVoicemail: Boolean? = null,
) {

    fun applyTo(details: ContactDetails): ContactDetails {
        return details.copy(
            isStarred = isStarred ?: details.isStarred,
            isSendToVoicemail = isSendToVoicemail ?: details.isSendToVoicemail,
        )
    }

    fun withoutApplied(details: ContactDetails): PendingContactFlags {
        return PendingContactFlags(
            isStarred = isStarred?.takeIf { pending ->
                pending != details.isStarred
            },
            isSendToVoicemail = isSendToVoicemail?.takeIf { pending ->
                pending != details.isSendToVoicemail
            },
        )
    }
}
