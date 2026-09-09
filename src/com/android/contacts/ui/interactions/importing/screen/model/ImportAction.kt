package com.android.contacts.ui.interactions.importing.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface ImportAction {
    data object Dismiss : ImportAction
    data object VCardClick : ImportAction
    data class SimOptionClick(
        val simCardOption: SimCardOption,
    ) : ImportAction
    data class AccountChosen(
        val account: AccountModel,
    ) : ImportAction
}
