package com.android.contacts.domain.accounts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 * Immutable domain model to match {@link com.android.contacts.model.account.AccountWithDataSet}
 */
@Parcelize
internal data class AccountModel(
    val name: String?,
    val type: String? = null,
    val dataSet: String? = null,
) : Parcelable
