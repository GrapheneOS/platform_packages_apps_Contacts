package com.android.contacts.ui.interactions.importing.screen.model

internal sealed interface ImportEffect {
    data object Close : ImportEffect
    data class OpenSimImport(
        val subscriptionId: Int,
    ) : ImportEffect
    data object OpenVCardImport : ImportEffect
}
