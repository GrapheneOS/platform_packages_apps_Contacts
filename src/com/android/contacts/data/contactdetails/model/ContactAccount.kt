package com.android.contacts.data.contactdetails.model

import com.android.contacts.domain.accounts.model.AccountIconData

internal data class ContactAccount(
    val name: String,
    val iconData: AccountIconData?,
)
