package com.android.contacts.ui.vcard.screen.model

import kotlinx.collections.immutable.ImmutableSet

internal sealed interface ImportVCardEffect {
    data class RequestPermissions(
        val permissions: ImmutableSet<String>,
    ) : ImportVCardEffect
    data object SelectFiles : ImportVCardEffect
    data object SelectAccount : ImportVCardEffect
    data class ShowImportError(
        val error: ImportVCardError,
    ) : ImportVCardEffect
    data object Close : ImportVCardEffect
}
