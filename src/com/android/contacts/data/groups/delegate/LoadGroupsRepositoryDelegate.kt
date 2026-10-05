package com.android.contacts.data.groups.delegate

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface LoadGroupsRepositoryDelegate {
    fun loadGroups(
        filters: List<GroupFilter> = emptyList(),
        sort: GroupSort = GroupSort.UNDEFINED,
    ): Flow<List<GroupColumn>?>
}

internal class LoadGroupsRepositoryDelegateImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadGroupsRepositoryDelegate {
    override fun loadGroups(
        filters: List<GroupFilter>,
        sort: GroupSort,
    ): Flow<List<GroupColumn>?> {
        return observeContentUri(contentResolver, ContactsContract.Groups.CONTENT_SUMMARY_URI)
            .map { load(filters, sort) }
            .flowOn(coroutineDispatcher)
    }

    private fun load(
        filters: List<GroupFilter>,
        sort: GroupSort,
    ): List<GroupColumn>? {
        return contentResolver.query(
            ContactsContract.Groups.CONTENT_SUMMARY_URI,
            GROUP_PROJECTION,
            selection(filters),
            selectionArgs(filters),
            sortOrder(sort),
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    buildGroupColumn(cursor)?.let(::add)
                }
            }
        }
    }

    private fun selection(filters: List<GroupFilter>): String {
        fun comparisonFor(value: String?): String {
            return when {
                value == null -> " IS NULL"
                else -> "=?"
            }
        }

        fun comparisonFor(value: Boolean): String {
            return "=" + if (value) "1" else "0"
        }

        return buildList {
            filters.forEach { filter ->
                when (filter) {
                    is GroupFilter.AutoAdd -> {
                        add("${ContactsContract.Groups.AUTO_ADD}${comparisonFor(filter.value)}")
                    }
                    is GroupFilter.ByAccount -> {
                        val account = filter.account
                        add("${ContactsContract.Groups.ACCOUNT_NAME}${comparisonFor(account.name)}")
                        add("${ContactsContract.Groups.ACCOUNT_TYPE}${comparisonFor(account.type)}")
                        add("${ContactsContract.Groups.DATA_SET}${comparisonFor(account.dataSet)}")
                    }
                    is GroupFilter.Favorites -> {
                        add("${ContactsContract.Groups.FAVORITES}${comparisonFor(filter.value)}")
                    }
                }
            }
            add("${ContactsContract.Groups.DELETED}=0")
        }.joinToString(" AND ")
    }

    private fun selectionArgs(filters: List<GroupFilter>): Array<String>? {
        return filters.flatMap { filter ->
            when (filter) {
                is GroupFilter.AutoAdd -> emptyList()
                is GroupFilter.ByAccount -> listOfNotNull(
                    filter.account.name,
                    filter.account.type,
                    filter.account.dataSet,
                )
                is GroupFilter.Favorites -> emptyList()
            }
        }.toTypedArray().takeIf { it.isNotEmpty() }
    }

    private fun sortOrder(sort: GroupSort): String? {
        return when (sort) {
            GroupSort.UNDEFINED -> null
            GroupSort.BY_TITLE -> "${ContactsContract.Groups.TITLE} COLLATE LOCALIZED ASC"
        }
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
        )
        private const val GROUP_ID = 0
        private const val GROUP_TITLE = 1
        private const val GROUP_SUMMARY_COUNT = 2
        private const val GROUP_SYSTEM_ID = 3
        private const val GROUP_ACCOUNT_NAME = 4
        private const val GROUP_ACCOUNT_TYPE = 5
        private const val GROUP_ACCOUNT_DATA_SET = 6
        private const val GROUP_IS_READ_ONLY = 7
    }
}
