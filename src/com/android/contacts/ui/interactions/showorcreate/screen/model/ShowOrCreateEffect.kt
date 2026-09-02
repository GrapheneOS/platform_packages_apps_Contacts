package com.android.contacts.ui.interactions.showorcreate.screen.model

import android.net.Uri
import android.os.Bundle

internal sealed interface ShowOrCreateEffect {
    data object Close : ShowOrCreateEffect

    data class ShowContact(
        val uri: Uri,
    ) : ShowOrCreateEffect

    data class ShowContactList(
        val extras: Bundle,
    ) : ShowOrCreateEffect

    data class CreateContact(
        val extras: Bundle,
    ) : ShowOrCreateEffect
}
