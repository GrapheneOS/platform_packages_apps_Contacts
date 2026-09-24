package com.android.contacts.domain.groups.model

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
import com.android.contacts.model.account.GoogleAccountType

internal data class Group(
    val id: Long,
    val name: String,
    val summaryCount: Int,
    val systemId: String?,
    val account: AccountModel?,
    val isReadOnly: Boolean,
) {
    val isEmptyFFCGroup: Boolean
        get() {
            return GoogleAccountType.ACCOUNT_TYPE == account?.type &&
                isReadOnly &&
                summaryCount <= 0 &&
                GroupUtil.isSystemIdFFC(systemId)
        }
}
