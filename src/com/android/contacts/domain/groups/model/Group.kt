package com.android.contacts.domain.groups.model

import com.android.contacts.domain.accounts.model.AccountModel

internal data class Group(
    val id: Long,
    val name: String,
    val summaryCount: Int,
    val systemId: String?,
    val account: AccountModel?,
    val isReadOnly: Boolean,
    val isDeleted: Boolean,
)
