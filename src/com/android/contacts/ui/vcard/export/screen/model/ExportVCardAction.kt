package com.android.contacts.ui.vcard.export.screen.model

import android.net.Uri

internal sealed interface ExportVCardAction {
    data object PermissionRequestFinished : ExportVCardAction
    data class ModeSelected(
        val mode: ExportMode?,
    ) : ExportVCardAction
    data class FileSelected(
        val uri: Uri?,
    ) : ExportVCardAction
}
