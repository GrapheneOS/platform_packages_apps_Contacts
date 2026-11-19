package com.android.contacts.data.groups.repository

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import androidx.annotation.VisibleForTesting
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.toContactColumns
import com.android.contacts.data.contacts.delegate.util.appendAddressBookIndex
import com.android.contacts.data.contacts.delegate.util.appendDefaultDirectory
import com.android.contacts.data.contacts.delegate.util.toSortKeyColumn
import com.android.contacts.data.contacts.model.AccountQuerySelection
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.groups.model.GroupSendTargetColumn
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.util.core.observeContentUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal interface GroupMembersRepository {

    fun queryGroupMembers(
        groupId: Long,
        sortOrder: SortOrder,
    ): Flow<List<ContactColumn>>

    suspend fun getSendTargets(
        contactIds: Set<Long>,
        kind: ContactDataKind,
    ): List<GroupSendTargetColumn>

    suspend fun getMembersCounts(groupIds: Collection<Long>): Map<Long, Int>?

    suspend fun getRawContactIds(
        contactIds: Set<Long>,
        account: AccountModel,
    ): List<Long>

    suspend fun updateGroupMembers(
        groupId: Long,
        rawContactIdsToAdd: List<Long>,
        rawContactIdsToRemove: List<Long>,
        callbackActivity: Class<out Activity>,
        callbackAction: String,
    )
}

internal class GroupMembersRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GroupMembersRepository {

    override fun queryGroupMembers(
        groupId: Long,
        sortOrder: SortOrder,
    ): Flow<List<ContactColumn>> {
        return observeContentUri(contentResolver, Contacts.CONTENT_URI)
            .map {
                contentResolver.query(
                    MEMBERS_URI,
                    QueryContactsRepositoryDelegateImpl.PROJECTION,
                    MEMBERS_SELECTION,
                    arrayOf(GroupMembership.CONTENT_ITEM_TYPE, groupId.toString()),
                    sortOrder.toSortKeyColumn(),
                )
                    ?.use { it.toContactColumns() }
                    .orEmpty()
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getSendTargets(
        contactIds: Set<Long>,
        kind: ContactDataKind,
    ): List<GroupSendTargetColumn> {
        if (contactIds.isEmpty()) return emptyList()

        return withContext(ioDispatcher) {
            contentResolver.query(
                Data.CONTENT_URI,
                SEND_TARGET_PROJECTION,
                "${Data.MIMETYPE}=? AND ${Data.CONTACT_ID} IN (${contactIds.joinToString(",")})",
                arrayOf(kind.mimeType()),
                null,
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val value = cursor.getString(SEND_TARGET_VALUE)
                        if (value.isNullOrBlank()) continue
                        add(
                            GroupSendTargetColumn(
                                contactId = cursor.getLong(SEND_TARGET_CONTACT_ID),
                                dataId = cursor.getLong(SEND_TARGET_DATA_ID),
                                value = value,
                                isSuperPrimary = cursor.getInt(SEND_TARGET_SUPER_PRIMARY) != 0,
                            ),
                        )
                    }
                }
            }.orEmpty()
        }
    }

    override suspend fun getMembersCounts(groupIds: Collection<Long>): Map<Long, Int>? {
        if (groupIds.isEmpty()) return emptyMap()

        return withContext(ioDispatcher) {
            contentResolver.query(
                Data.CONTENT_URI,
                MEMBERS_COUNT_PROJECTION,
                "${Data.MIMETYPE}=? AND " +
                    "${GroupMembership.GROUP_ROW_ID} IN (${groupIds.joinToString(",")})",
                arrayOf(GroupMembership.CONTENT_ITEM_TYPE),
                null,
            )?.use { cursor ->
                val contactIdsByGroup = mutableMapOf<Long, MutableSet<Long>>()
                while (cursor.moveToNext()) {
                    val groupId = cursor.getLong(MEMBERS_COUNT_GROUP_ID)
                    val contactId = cursor.getLong(MEMBERS_COUNT_CONTACT_ID)
                    contactIdsByGroup
                        .getOrPut(groupId) { mutableSetOf() }
                        .add(contactId)
                }
                contactIdsByGroup.mapValues { (_, contactIds) -> contactIds.size }
            }
        }
    }

    override suspend fun getRawContactIds(
        contactIds: Set<Long>,
        account: AccountModel,
    ): List<Long> {
        val accountSelection = AccountQuerySelection.forAccount(account)
        if (contactIds.isEmpty() || accountSelection == AccountQuerySelection.None) {
            return emptyList()
        }

        return withContext(ioDispatcher) {
            contentResolver.query(
                RawContacts.CONTENT_URI,
                arrayOf(RawContacts._ID),
                "${RawContacts.CONTACT_ID} IN (${contactIds.joinToString(",")}) AND " +
                    "(${accountSelection.rawContactSelection})",
                accountSelection.rawContactSelectionArgs
                    .takeIf { it.isNotEmpty() }
                    ?.toTypedArray(),
                null,
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getLong(0))
                    }
                }
            }.orEmpty()
        }
    }

    override suspend fun updateGroupMembers(
        groupId: Long,
        rawContactIdsToAdd: List<Long>,
        rawContactIdsToRemove: List<Long>,
        callbackActivity: Class<out Activity>,
        callbackAction: String,
    ) {
        withContext(ioDispatcher) {
            val intent = ContactSaveService.createGroupUpdateIntent(
                context,
                groupId,
                null,
                rawContactIdsToAdd.takeIf { it.isNotEmpty() }?.toLongArray(),
                rawContactIdsToRemove.takeIf { it.isNotEmpty() }?.toLongArray(),
                callbackActivity,
                callbackAction,
            )
            ContactSaveService.startService(context, intent)
        }
    }

    private fun ContactDataKind.mimeType(): String {
        return when (this) {
            is ContactDataKind.Phone -> Phone.CONTENT_ITEM_TYPE
            ContactDataKind.Email -> Email.CONTENT_ITEM_TYPE
            ContactDataKind.PostalAddress -> StructuredPostal.CONTENT_ITEM_TYPE
        }
    }

    companion object {
        private val MEMBERS_URI: Uri = Contacts.CONTENT_URI.buildUpon()
            .appendAddressBookIndex()
            .appendDefaultDirectory()
            .build()

        @VisibleForTesting
        const val MEMBERS_SELECTION =
            Contacts._ID + " IN (" +
                "SELECT ${Data.CONTACT_ID} FROM view_data WHERE " +
                "${Data.MIMETYPE}=? AND ${GroupMembership.GROUP_ROW_ID}=?" +
                ")"

        private val MEMBERS_COUNT_PROJECTION = arrayOf(
            GroupMembership.GROUP_ROW_ID,
            Data.CONTACT_ID,
        )
        private const val MEMBERS_COUNT_GROUP_ID = 0
        private const val MEMBERS_COUNT_CONTACT_ID = 1

        private val SEND_TARGET_PROJECTION = arrayOf(
            Data.CONTACT_ID,
            Data._ID,
            Data.IS_SUPER_PRIMARY,
            Data.DATA1,
        )
        private const val SEND_TARGET_CONTACT_ID = 0
        private const val SEND_TARGET_DATA_ID = 1
        private const val SEND_TARGET_SUPER_PRIMARY = 2
        private const val SEND_TARGET_VALUE = 3
    }
}
