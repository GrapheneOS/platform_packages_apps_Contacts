package com.android.contacts.data.contacts.model

import com.android.contacts.domain.accounts.model.AccountModel

internal sealed interface AccountQuerySelection {

    data object None : AccountQuerySelection

    data object Local : AccountQuerySelection

    data class WithoutDataSet(
        val name: String,
        val type: String,
    ) : AccountQuerySelection

    data class WithDataSet(
        val name: String,
        val type: String,
        val dataSet: String,
    ) : AccountQuerySelection

    companion object {
        fun forAccount(account: AccountModel?): AccountQuerySelection {
            return when {
                account == null -> None
                account.isNullAccount -> Local
                account.name == null || account.type == null -> None
                account.dataSet == null -> WithoutDataSet(account.name, account.type)
                else -> WithDataSet(account.name, account.type, account.dataSet)
            }
        }
    }
}
