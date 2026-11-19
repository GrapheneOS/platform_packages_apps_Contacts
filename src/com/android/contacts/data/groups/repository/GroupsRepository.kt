package com.android.contacts.data.groups.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.ContactsContract
import com.android.contacts.ContactSaveService
import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.groups.model.GroupDeletion
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import com.android.contacts.util.core.observeContentUri
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal interface GroupsRepository {

    fun loadGroups(
        filters: List<GroupFilter> = emptyList(),
        sort: GroupSort = GroupSort.UNDEFINED,
    ): Flow<List<GroupColumn>?>

    fun loadGroup(groupId: Long): Flow<GroupColumn?>

    suspend fun deleteGroup(groupId: Long): GroupDeletion?

    suspend fun restoreGroup(deletion: GroupDeletion): Boolean
}

internal class GroupsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val groupsDao: ContactSaveService.GroupsDao,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : GroupsRepository {

    override fun loadGroups(
        filters: List<GroupFilter>,
        sort: GroupSort,
    ): Flow<List<GroupColumn>?> {
        return observeContentUri(contentResolver, ContactsContract.Groups.CONTENT_URI)
            .map { queryGroups(filters, sort) }
            .flowOn(coroutineDispatcher)
    }

    override fun loadGroup(groupId: Long): Flow<GroupColumn?> {
        return observeContentUri(contentResolver, ContactsContract.Groups.CONTENT_URI)
            .map {
                query(
                    selection = "${ContactsContract.Groups._ID}=? AND " +
                        "${ContactsContract.Groups.DELETED}=0",
                    selectionArgs = arrayOf(groupId.toString()),
                )?.firstOrNull()
            }
            .flowOn(coroutineDispatcher)
    }

    override suspend fun deleteGroup(groupId: Long): GroupDeletion? {
        return withContext(coroutineDispatcher) {
            val groupUri = ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, groupId)
            val undoData = groupsDao.captureDeletionUndoData(groupUri)

            when (groupsDao.delete(groupUri)) {
                0 -> null
                else -> GroupDeletion(undoData = undoData)
            }
        }
    }

    override suspend fun restoreGroup(deletion: GroupDeletion): Boolean {
        return withContext(coroutineDispatcher) {
            groupsDao.undoDeletion(deletion.undoData) != null
        }
    }

    private fun queryGroups(
        filters: List<GroupFilter>,
        sort: GroupSort,
    ): List<GroupColumn>? {
        return query(
            selection = selection(filters),
            selectionArgs = selectionArgs(filters),
            sortOrder = sortOrder(sort),
        )
    }

    private fun query(
        selection: String,
        selectionArgs: Array<String>?,
        sortOrder: String? = null,
    ): List<GroupColumn>? {
        return contentResolver.query(
            ContactsContract.Groups.CONTENT_URI,
            GROUP_PROJECTION,
            selection,
            selectionArgs,
            sortOrder,
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
            title = cursor.getString(GROUP_TITLE)?.takeIf { it.isNotEmpty() } ?: return null,
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
            ContactsContract.Groups.SYSTEM_ID,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.ACCOUNT_TYPE,
            ContactsContract.Groups.DATA_SET,
            ContactsContract.Groups.GROUP_IS_READ_ONLY,
        )
        private const val GROUP_ID = 0
        private const val GROUP_TITLE = 1
        private const val GROUP_SYSTEM_ID = 2
        private const val GROUP_ACCOUNT_NAME = 3
        private const val GROUP_ACCOUNT_TYPE = 4
        private const val GROUP_ACCOUNT_DATA_SET = 5
        private const val GROUP_IS_READ_ONLY = 6
    }
}
