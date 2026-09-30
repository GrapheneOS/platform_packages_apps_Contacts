package com.android.contacts.ui.vcardimport.screen.model

import com.android.contacts.domain.vcard.model.ImportVCardError
import kotlinx.collections.immutable.ImmutableSet

internal sealed interface ImportVCardEffect {

    sealed interface ActionRequired : ImportVCardEffect
    sealed interface OneOff : ImportVCardEffect

    data class RequestPermissions(
        val permissions: ImmutableSet<String>,
    ) : ActionRequired

    data object SelectFiles : ActionRequired

    data object SelectAccount : ActionRequired

    data class ShowImportError(
        val error: ImportVCardError,
    ) : OneOff

    data object Close : OneOff
}
