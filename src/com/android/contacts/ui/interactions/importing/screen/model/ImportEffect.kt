package com.android.contacts.ui.interactions.importing.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface ImportEffect {
    data object Close : ImportEffect
    data class OpenSimImport(
        val subscriptionId: Int,
    ) : ImportEffect
    data class OpenVCardImport(
        val account: AccountModel?,
    ) : ImportEffect
    data object OpenSelectAccount : ImportEffect
}
