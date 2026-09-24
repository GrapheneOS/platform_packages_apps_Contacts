package com.android.contacts.domain.groups.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface GroupFilter {

    data class ByAccount(
        val account: AccountModel,
    ) : GroupFilter

    data class AutoAdd(
        val value: Boolean,
    ) : GroupFilter

    data class Favorites(
        val value: Boolean,
    ) : GroupFilter
}
