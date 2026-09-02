package com.android.contacts.ui.vcardexport.screen.model

import kotlinx.collections.immutable.ImmutableSet

internal sealed interface ExportVCardEffect {

    data class RequestPermissions(
        val permissions: ImmutableSet<String>,
    ) : ExportVCardEffect

    data object SelectFile : ExportVCardEffect

    data object ShowError : ExportVCardEffect

    data object Close : ExportVCardEffect
}
