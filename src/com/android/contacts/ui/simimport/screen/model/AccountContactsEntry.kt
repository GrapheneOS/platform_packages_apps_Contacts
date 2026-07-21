package com.android.contacts.ui.simimport.screen.model

import android.os.Parcelable
import com.android.contacts.domain.accounts.model.AccountModel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AccountContactsEntry(
    val account: AccountModel,
    val contactNumbers: Set<Int>,
) : Parcelable
