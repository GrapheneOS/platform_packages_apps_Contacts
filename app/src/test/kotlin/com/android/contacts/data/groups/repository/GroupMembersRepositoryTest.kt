package com.android.contacts.data.groups.repository

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import app.cash.turbine.test
import com.android.contacts.ContactSaveService
import com.android.contacts.data.contacts.delegate.QueryContactsRepositoryDelegateImpl.Companion.PROJECTION
import com.android.contacts.data.contacts.model.AccountQuerySelection.Companion.LOCAL_RAW_CONTACT_SELECTION
import com.android.contacts.data.contacts.model.AccountQuerySelection.Companion.WITHOUT_DATA_SET_RAW_CONTACT_SELECTION
import com.android.contacts.data.contacts.model.AccountQuerySelection.Companion.WITH_DATA_SET_RAW_CONTACT_SELECTION
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.groups.model.GroupSendTargetColumn
import com.android.contacts.data.groups.repository.GroupMembersRepositoryImpl.Companion.MEMBERS_SELECTION
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.tests.givenQueryResult
import com.android.contacts.data.tests.givenQueryRows
import com.android.contacts.data.tests.givenRegisteredContentObserver
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.factory.AccountModelFactory
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupMembersRepositoryTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject: GroupMembersRepository = GroupMembersRepositoryImpl(
        context = context,
        contentResolver = contentResolver,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun queryGroupMembers_queriesTheGroupMembers() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryGroupMembers(groupId = 7L, sortOrder = SortOrder.GIVEN_NAME_FIRST).first()

        val query = queries.single()
        assertEquals(
            Contacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true")
                .appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY,
                    ContactsContract.Directory.DEFAULT.toString(),
                )
                .build(),
            query.uri,
        )
        assertArrayEquals(PROJECTION, query.projection)
        assertEquals(MEMBERS_SELECTION, query.selection)
        assertArrayEquals(arrayOf(GroupMembership.CONTENT_ITEM_TYPE, "7"), query.selectionArgs)
        assertEquals(Contacts.SORT_KEY_PRIMARY, query.sortOrder)
    }

    @Test
    fun queryGroupMembers_withFamilyNameFirst_sortsByAlternativeKey() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.queryGroupMembers(groupId = 7L, sortOrder = SortOrder.FAMILY_NAME_FIRST).first()

        assertEquals(Contacts.SORT_KEY_ALTERNATIVE, queries.single().sortOrder)
    }

    @Test
    fun queryGroupMembers_mapsRows() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, "Alice", "Alice", null, 1),
            arrayOf(2L, null, null, null, 0),
        )

        val result = subject.queryGroupMembers(7L, SortOrder.GIVEN_NAME_FIRST).first()

        assertEquals(
            listOf(
                ContactColumn(
                    id = 1L,
                    displayNamePrimary = "Alice",
                    displayNameAlternative = "Alice",
                    isStarred = true,
                ),
                ContactColumn(id = 2L),
            ),
            result,
        )
    }

    @Test
    fun queryGroupMembers_whenCursorIsNull_emitsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        val result = subject.queryGroupMembers(7L, SortOrder.GIVEN_NAME_FIRST).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun queryGroupMembers_whenContactsChange_emitsAgain() = runTest {
        val observerSlot = contentResolver.givenRegisteredContentObserver()
        contentResolver.givenQueryRows(arrayOf(1L, "Alice", null, null, 0))

        subject.queryGroupMembers(7L, SortOrder.GIVEN_NAME_FIRST).test {
            assertEquals(1L, awaitItem().single().id)

            contentResolver.givenQueryRows(arrayOf(2L, "Bob", null, null, 0))
            observerSlot.captured.onChange(false)

            assertEquals(2L, awaitItem().single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSendTargets_withoutContacts_doesNotQuery() = runTest {
        val result = subject.getSendTargets(emptySet(), ContactDataKind.Email)

        assertTrue(result.isEmpty())
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getSendTargets_queriesTheKindDataOfTheContacts() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getSendTargets(setOf(1L, 2L), ContactDataKind.Phone())

        val query = queries.single()
        assertEquals(Data.CONTENT_URI, query.uri)
        assertArrayEquals(
            arrayOf(Data.CONTACT_ID, Data._ID, Data.IS_SUPER_PRIMARY, Data.DATA1),
            query.projection,
        )
        assertEquals("${Data.MIMETYPE}=? AND ${Data.CONTACT_ID} IN (1,2)", query.selection)
        assertArrayEquals(arrayOf(Phone.CONTENT_ITEM_TYPE), query.selectionArgs)
        assertNull(query.sortOrder)
    }

    @Test
    fun getSendTargets_usesTheMimeTypeOfTheKind() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getSendTargets(setOf(1L), ContactDataKind.Email)
        subject.getSendTargets(setOf(1L), ContactDataKind.PostalAddress)

        assertEquals(
            listOf(Email.CONTENT_ITEM_TYPE, StructuredPostal.CONTENT_ITEM_TYPE),
            queries.map { it.selectionArgs?.single() },
        )
    }

    @Test
    fun getSendTargets_mapsRowsAndSkipsBlankValues() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(1L, 10L, 1, "111"),
            arrayOf(1L, 11L, 0, " "),
            arrayOf(2L, 20L, 0, null),
            arrayOf(3L, 30L, 0, "333"),
        )

        val result = subject.getSendTargets(setOf(1L, 2L, 3L), ContactDataKind.Phone())

        assertEquals(
            listOf(
                GroupSendTargetColumn(
                    contactId = 1L,
                    dataId = 10L,
                    value = "111",
                    isSuperPrimary = true,
                ),
                GroupSendTargetColumn(
                    contactId = 3L,
                    dataId = 30L,
                    value = "333",
                    isSuperPrimary = false,
                ),
            ),
            result,
        )
    }

    @Test
    fun getSendTargets_whenCursorIsNull_returnsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        assertTrue(subject.getSendTargets(setOf(1L), ContactDataKind.Email).isEmpty())
    }

    @Test
    fun getMembersCounts_withoutGroups_doesNotQuery() = runTest {
        val result = subject.getMembersCounts(emptyList())

        assertEquals(emptyMap<Long, Int>(), result)
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getMembersCounts_queriesTheGroupMemberships() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getMembersCounts(listOf(7L, 8L))

        val query = queries.single()
        assertEquals(Data.CONTENT_URI, query.uri)
        assertEquals(
            "${Data.MIMETYPE}=? AND ${GroupMembership.GROUP_ROW_ID} IN (7,8)",
            query.selection,
        )
        assertArrayEquals(arrayOf(GroupMembership.CONTENT_ITEM_TYPE), query.selectionArgs)
    }

    @Test
    fun getMembersCounts_countsDistinctContactsPerGroup() = runTest {
        contentResolver.givenQueryRows(
            arrayOf(7L, 1L),
            arrayOf(7L, 1L),
            arrayOf(7L, 2L),
            arrayOf(8L, 1L),
        )

        val result = subject.getMembersCounts(listOf(7L, 8L, 9L))

        assertEquals(mapOf(7L to 2, 8L to 1), result)
    }

    @Test
    fun getMembersCounts_whenCursorIsNull_returnsNull() = runTest {
        contentResolver.givenQueryResult { null }

        assertNull(subject.getMembersCounts(listOf(7L)))
    }

    @Test
    fun getRawContactIds_withoutContacts_doesNotQuery() = runTest {
        val result = subject.getRawContactIds(emptySet(), ACCOUNT)

        assertTrue(result.isEmpty())
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getRawContactIds_withIncompleteAccount_doesNotQuery() = runTest {
        val result = subject.getRawContactIds(
            setOf(1L),
            AccountModelFactory.build(name = null, type = "com.example"),
        )

        assertTrue(result.isEmpty())
        verify(exactly = 0) { contentResolver.query(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getRawContactIds_withAccount_selectsTheAccountRawContacts() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getRawContactIds(setOf(1L, 2L), ACCOUNT)

        val query = queries.single()
        assertEquals(RawContacts.CONTENT_URI, query.uri)
        assertArrayEquals(arrayOf(RawContacts._ID), query.projection)
        assertEquals(
            "${RawContacts.CONTACT_ID} IN (1,2) AND ($WITHOUT_DATA_SET_RAW_CONTACT_SELECTION)",
            query.selection,
        )
        assertArrayEquals(arrayOf("user@example.org", "com.example"), query.selectionArgs)
    }

    @Test
    fun getRawContactIds_withDataSet_selectsTheDataSet() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getRawContactIds(setOf(1L), ACCOUNT.copy(dataSet = "plus"))

        val query = queries.single()
        assertEquals(
            "${RawContacts.CONTACT_ID} IN (1) AND ($WITH_DATA_SET_RAW_CONTACT_SELECTION)",
            query.selection,
        )
        assertArrayEquals(arrayOf("user@example.org", "com.example", "plus"), query.selectionArgs)
    }

    @Test
    fun getRawContactIds_withNullAccount_selectsLocalRawContacts() = runTest {
        val queries = contentResolver.givenQueryRows()

        subject.getRawContactIds(setOf(1L), AccountModel.NULL)

        val query = queries.single()
        assertEquals(
            "${RawContacts.CONTACT_ID} IN (1) AND ($LOCAL_RAW_CONTACT_SELECTION)",
            query.selection,
        )
        assertNull(query.selectionArgs)
    }

    @Test
    fun getRawContactIds_mapsRows() = runTest {
        contentResolver.givenQueryRows(arrayOf(10L), arrayOf(11L))

        val result = subject.getRawContactIds(setOf(1L), ACCOUNT)

        assertEquals(listOf(10L, 11L), result)
    }

    @Test
    fun getRawContactIds_whenCursorIsNull_returnsEmptyList() = runTest {
        contentResolver.givenQueryResult { null }

        assertTrue(subject.getRawContactIds(setOf(1L), ACCOUNT).isEmpty())
    }

    @Test
    fun updateGroupMembers_startsTheGroupUpdateService() = runTest {
        subject.updateGroupMembers(
            groupId = 7L,
            rawContactIdsToAdd = listOf(10L, 11L),
            rawContactIdsToRemove = listOf(20L),
            callbackActivity = Activity::class.java,
            callbackAction = GroupUtil.ACTION_ADD_TO_GROUP,
        )

        val intent = shadowOf(RuntimeEnvironment.getApplication()).nextStartedService
        assertEquals(ContactSaveService::class.java.name, intent.component?.className)
        assertEquals(ContactSaveService.ACTION_UPDATE_GROUP, intent.action)
        assertEquals(7L, intent.getLongExtra(ContactSaveService.EXTRA_GROUP_ID, -1L))
        assertArrayEquals(
            longArrayOf(10L, 11L),
            intent.getLongArrayExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD),
        )
        assertArrayEquals(
            longArrayOf(20L),
            intent.getLongArrayExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_REMOVE),
        )
        val callback = intent.callbackIntent()
        assertEquals(Activity::class.java.name, callback?.component?.className)
        assertEquals(GroupUtil.ACTION_ADD_TO_GROUP, callback?.action)
    }

    @Test
    fun updateGroupMembers_withEmptyLists_omitsTheRawContactIds() = runTest {
        subject.updateGroupMembers(
            groupId = 7L,
            rawContactIdsToAdd = emptyList(),
            rawContactIdsToRemove = emptyList(),
            callbackActivity = Activity::class.java,
            callbackAction = GroupUtil.ACTION_REMOVE_FROM_GROUP,
        )

        val intent = shadowOf(RuntimeEnvironment.getApplication()).nextStartedService
        assertNull(intent.getLongArrayExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD))
        assertNull(intent.getLongArrayExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_REMOVE))
    }

    @Suppress("DEPRECATION")
    private fun Intent.callbackIntent(): Intent? {
        return getParcelableExtra(ContactSaveService.EXTRA_CALLBACK_INTENT)
    }

    private companion object {
        val ACCOUNT = AccountModelFactory.build(name = "user@example.org", type = "com.example")
    }
}
