package com.android.contacts.domain.accounts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 * Immutable domain model to match {@link com.android.contacts.model.account.AccountDisplayInfo}
 */
@Parcelize
internal data class AccountDisplayModel(
    val account: AccountModel,
    val name: String?,
    val type: String? = null,
    val iconData: AccountIconData? = null,
    val isDeviceAccount: Boolean = true,
    val areContactsWritable: Boolean,
) : Parcelable
