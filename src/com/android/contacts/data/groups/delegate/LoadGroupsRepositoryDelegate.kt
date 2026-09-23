package com.android.contacts.data.groups.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface LoadGroupsRepositoryDelegate {
    fun loadGroups(
        account: AccountModel?,
    ): Flow<List<GroupColumn>?>
}

internal class LoadGroupsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadGroupsRepositoryDelegate {
    override fun loadGroups(account: AccountModel?): Flow<List<GroupColumn>?> {
        return observeContentUri(contentResolver, ContactsContract.Groups.CONTENT_SUMMARY_URI)
            .map { load(account) }
            .flowOn(coroutineDispatcher)
    }

    private fun load(account: AccountModel?): List<GroupColumn>? {
        return contentResolver.query(
            ContactsContract.Groups.CONTENT_SUMMARY_URI,
            GROUP_PROJECTION,
            selection(account),
            selectionArgs(account),
            null,
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    buildGroupColumn(cursor)?.let(::add)
                }
            }
        }
    }

    private fun selection(account: AccountModel?): String? {
        if (account == null) return null

        fun comparisonFor(field: String?): String {
            return when {
                field == null -> "IS NULL"
                else -> "=?"
            }
        }

        return listOf(
            "${ContactsContract.Groups.ACCOUNT_NAME} ${comparisonFor(account.name)}",
            "${ContactsContract.Groups.ACCOUNT_TYPE} ${comparisonFor(account.type)}",
            "${ContactsContract.Groups.DATA_SET} ${comparisonFor(account.dataSet)}",
        ).joinToString(" AND ")
    }

    private fun selectionArgs(account: AccountModel?): Array<String>? {
        if (account == null) return null
        return listOfNotNull(account.name, account.type, account.dataSet)
            .toTypedArray()
    }

    private fun buildGroupColumn(cursor: Cursor): GroupColumn? {
        return GroupColumn(
            id = cursor.getLong(GROUP_ID),
            title = cursor.getString(GROUP_TITLE).takeIf { it.isNotEmpty() } ?: return null,
            summaryCount = cursor.getInt(GROUP_SUMMARY_COUNT),
            systemId = cursor.getString(GROUP_SYSTEM_ID),
            accountName = cursor.getString(GROUP_ACCOUNT_NAME),
            accountType = cursor.getString(GROUP_ACCOUNT_TYPE),
            accountDataSet = cursor.getString(GROUP_ACCOUNT_DATA_SET),
            isReadOnly = cursor.getInt(GROUP_IS_READ_ONLY) == 1,
            isDeleted = cursor.getInt(GROUP_DELETED) == 1,
        )
    }

    companion object {
        private val GROUP_PROJECTION = arrayOf(
            ContactsContract.Groups._ID,
            ContactsContract.Groups.TITLE,
            ContactsContract.Groups.SUMMARY_COUNT,
            ContactsContract.Groups.SYSTEM_ID,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.ACCOUNT_TYPE,
            ContactsContract.Groups.DATA_SET,
            ContactsContract.Groups.GROUP_IS_READ_ONLY,
            ContactsContract.Groups.DELETED,
        )
        private const val GROUP_ID = 0
        private const val GROUP_TITLE = 1
        private const val GROUP_SUMMARY_COUNT = 2
        private const val GROUP_SYSTEM_ID = 3
        private const val GROUP_ACCOUNT_NAME = 4
        private const val GROUP_ACCOUNT_TYPE = 5
        private const val GROUP_ACCOUNT_DATA_SET = 6
        private const val GROUP_IS_READ_ONLY = 7
        private const val GROUP_DELETED = 8
    }
}
