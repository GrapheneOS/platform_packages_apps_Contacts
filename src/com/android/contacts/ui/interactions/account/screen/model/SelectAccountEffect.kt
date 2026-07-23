package com.android.contacts.ui.interactions.account.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface SelectAccountEffect {
    data class Close(
        val account: AccountModel?,
    ) : SelectAccountEffect
}
