package com.android.contacts.tests.factory

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.Group

internal object GroupFactory {
    fun build(
        id: Long = 1L,
        name: String = "Group",
        systemId: String? = null,
        account: AccountModel = AccountModelFactory.build(),
        isReadOnly: Boolean = false,
        membersCount: Int? = null,
    ) = Group(
        id = id,
        name = name,
        systemId = systemId,
        account = account,
        isReadOnly = isReadOnly,
        membersCount = membersCount,
    )
}
