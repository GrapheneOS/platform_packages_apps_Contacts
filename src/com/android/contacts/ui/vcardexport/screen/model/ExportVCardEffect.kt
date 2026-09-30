package com.android.contacts.ui.vcardexport.screen.model

import kotlinx.collections.immutable.ImmutableSet

internal sealed interface ExportVCardEffect {

    sealed interface ActionRequired : ExportVCardEffect
    sealed interface OneOff : ExportVCardEffect

    data class RequestPermissions(
        val permissions: ImmutableSet<String>,
    ) : ActionRequired

    data object SelectFile : ActionRequired

    data object ShowError : OneOff

    data object Close : OneOff
}
