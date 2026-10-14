package com.android.contacts.tests.factory

import com.android.contacts.data.groups.model.GroupColumn

internal object GroupColumnFactory {
    fun build(
        id: Long = 1L,
        title: String = "Group",
        summaryCount: Int = 0,
        systemId: String? = null,
        accountName: String? = null,
        accountType: String? = null,
        accountDataSet: String? = null,
        isReadOnly: Boolean = false,
        isDeleted: Boolean = false,
    ) = GroupColumn(
        id = id,
        title = title,
        summaryCount = summaryCount,
        systemId = systemId,
        accountName = accountName,
        accountType = accountType,
        accountDataSet = accountDataSet,
        isReadOnly = isReadOnly,
        isDeleted = isDeleted,
    )
}
