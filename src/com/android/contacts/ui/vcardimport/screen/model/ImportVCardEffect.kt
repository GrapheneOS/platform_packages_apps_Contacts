package com.android.contacts.ui.vcardimport.screen.model

import com.android.contacts.domain.vcard.model.ImportVCardError
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
