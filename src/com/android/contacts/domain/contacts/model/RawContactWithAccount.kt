package com.android.contacts.domain.contacts.model

import android.os.Parcelable
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class RawContactWithAccount(
    val id: Long,
    val photoUri: String?,
    val displayName: String?,
    val displayNameAlt: String?,
    val account: AccountDisplayModel,
) : Parcelable
