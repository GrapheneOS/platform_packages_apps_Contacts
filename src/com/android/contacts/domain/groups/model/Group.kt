package com.android.contacts.domain.groups.model

import com.android.contacts.domain.accounts.model.AccountModel

internal data class Group(
    val id: Long,
    val name: String,
    val systemId: String?,
    val account: AccountModel,
    val isReadOnly: Boolean,
    val membersCount: Int? = null,
)
