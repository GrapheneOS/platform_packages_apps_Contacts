package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.Group

internal object GroupFactory {
    fun build(
        id: Long = 1L,
        name: String = "Group",
        summaryCount: Int = 0,
        systemId: String? = null,
        account: AccountModel? = null,
        isReadOnly: Boolean = false,
        isDeleted: Boolean = false,
    ) = Group(
        id = id,
        name = name,
        summaryCount = summaryCount,
        systemId = systemId,
        account = account,
        isReadOnly = isReadOnly,
        isDeleted = isDeleted,
    )
}
