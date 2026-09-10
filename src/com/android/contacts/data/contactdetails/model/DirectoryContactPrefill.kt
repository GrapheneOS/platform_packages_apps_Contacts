package com.android.contacts.data.contactdetails.model

import android.accounts.Account
import android.content.ContentValues

internal data class DirectoryContactPrefill(
    val name: String?,
    val values: List<ContentValues>,
    val account: Account?,
    val dataSet: String?,
)
