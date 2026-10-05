package com.android.contacts.domain.groups.mapper

import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.Group
import javax.inject.Inject

internal interface GroupMapper {
    fun map(groupColumn: GroupColumn): Group
}

internal class GroupMapperImpl @Inject constructor() : GroupMapper {
    override fun map(groupColumn: GroupColumn): Group {
        return Group(
            id = groupColumn.id,
            name = groupColumn.title,
            summaryCount = groupColumn.summaryCount ?: 0,
            systemId = groupColumn.systemId,
            account = AccountModel(
                name = groupColumn.accountName,
                type = groupColumn.accountType,
                dataSet = groupColumn.accountDataSet,
            ).takeIf { !it.isNullAccount },
            isReadOnly = groupColumn.isReadOnly,
        )
    }
}
