package com.android.contacts.data.contacts.model

import android.provider.ContactsContract.RawContacts
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

    val rawContactSelection: String?
        get() = when (this) {
            None -> null
            Local -> LOCAL_RAW_CONTACT_SELECTION
            is WithoutDataSet -> WITHOUT_DATA_SET_RAW_CONTACT_SELECTION
            is WithDataSet -> WITH_DATA_SET_RAW_CONTACT_SELECTION
        }

    val rawContactSelectionArgs: List<String>
        get() = when (this) {
            None,
            Local,
            -> emptyList()
            is WithoutDataSet -> listOf(name, type)
            is WithDataSet -> listOf(name, type, dataSet)
        }

    companion object {
        const val LOCAL_RAW_CONTACT_SELECTION =
            RawContacts.ACCOUNT_TYPE + " IS NULL AND " +
                RawContacts.ACCOUNT_NAME + " IS NULL AND " +
                RawContacts.DATA_SET + " IS NULL"

        private const val ACCOUNT_RAW_CONTACT_SELECTION_PREFIX =
            RawContacts.ACCOUNT_NAME + "=? AND " +
                RawContacts.ACCOUNT_TYPE + "=? AND " +
                RawContacts.DATA_SET

        const val WITHOUT_DATA_SET_RAW_CONTACT_SELECTION =
            "$ACCOUNT_RAW_CONTACT_SELECTION_PREFIX IS NULL"

        const val WITH_DATA_SET_RAW_CONTACT_SELECTION = "$ACCOUNT_RAW_CONTACT_SELECTION_PREFIX=?"

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
