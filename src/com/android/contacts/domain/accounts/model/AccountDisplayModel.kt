package com.android.contacts.domain.accounts.model

/*
 * Immutable domain model to match {@link com.android.contacts.model.account.AccountDisplayInfo}
 */
internal data class AccountDisplayModel(
    val account: AccountModel,
    val name: String?,
    val type: String? = null,
    val iconData: AccountIconData? = null,
    val isDeviceAccount: Boolean = true,
)
