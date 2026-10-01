package com.android.contacts.ui.interactions.account.filter.screen.model

internal sealed interface AccountFilterAction {

    data object Dismiss : AccountFilterAction

    data class ItemClicked(
        val item: AccountFilterItem,
    ) : AccountFilterAction
}
