package com.android.contacts.ui.vcard.screen.model

import android.net.Uri
import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface ImportVCardAction {
    data object PermissionRequestFinished : ImportVCardAction
    data class FilesSelected(
        val uris: List<Uri>?,
    ) : ImportVCardAction
    data class AccountSelected(
        val account: AccountModel?,
    ) : ImportVCardAction
    data object CancelClicked : ImportVCardAction
    data object FailureDismissed : ImportVCardAction
}
