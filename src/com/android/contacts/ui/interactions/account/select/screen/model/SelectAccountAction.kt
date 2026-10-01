package com.android.contacts.ui.interactions.account.select.screen.model

import com.android.contacts.ui.simimport.screen.model.AccountUiModel

internal sealed interface SelectAccountAction {
    data object Dismiss : SelectAccountAction
    data class AccountSelected(
        val account: AccountUiModel,
    ) : SelectAccountAction
}
