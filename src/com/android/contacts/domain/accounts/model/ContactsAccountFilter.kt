package com.android.contacts.domain.accounts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface ContactsAccountFilter : Parcelable {

    data object All : ContactsAccountFilter

    data class One(
        val account: AccountModel,
    ) : ContactsAccountFilter
}
