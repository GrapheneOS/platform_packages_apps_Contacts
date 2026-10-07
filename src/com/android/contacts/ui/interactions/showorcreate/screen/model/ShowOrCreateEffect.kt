package com.android.contacts.ui.interactions.showorcreate.screen.model

import android.net.Uri
import com.android.contacts.data.contacts.model.ContactLookupQuery

internal sealed interface ShowOrCreateEffect {
    data object Close : ShowOrCreateEffect

    data class ShowContact(
        val uri: Uri,
    ) : ShowOrCreateEffect

    data class ShowContactList(
        val query: ContactLookupQuery,
    ) : ShowOrCreateEffect

    data class CreateContact(
        val query: ContactLookupQuery,
    ) : ShowOrCreateEffect

    data class CreateOrEditContact(
        val query: ContactLookupQuery,
    ) : ShowOrCreateEffect
}
