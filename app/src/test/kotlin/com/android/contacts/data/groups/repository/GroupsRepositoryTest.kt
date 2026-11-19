package com.android.contacts.data.groups.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.ContactSaveService
import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.GroupDeletion
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupsRepositoryTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)
    private val groupsDao = mockk<ContactSaveService.GroupsDao>()

    private val subject: GroupsRepository = GroupsRepositoryImpl(
        contentResolver = contentResolver,
        groupsDao = groupsDao,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun whenEmptyRows_returnsEmptyList() = runTest {
        givenQueryRows()

        val result = subject.loadGroups().first()!!

        assertEquals(emptyList<GroupColumn>(), result)
    }

    @Test
    fun returnsCorrectFields() = runTest {
        givenQueryRows(
            arrayOf(
                123L,
                "Group Name",
                "Group System Id",
                "Account Name",
                "Device",
                null,
                0,
            ),
        )

        val result = subject.loadGroups().first()!!

        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(123L, id)
            assertEquals("Group Name", title)
            assertEquals("Group System Id", systemId)
            assertEquals("Account Name", accountName)
            assertEquals("Device", accountType)
            assertNull(accountDataSet)
            assertFalse(isReadOnly)
        }
    }

    @Test
    fun withoutFilters_querySelectionIsJustDeleted() = runTest {
        subject.loadGroups(filters = emptyList()).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                "${ContactsContract.Groups.DELETED}=0",
                null,
                any(),
            )
        }
    }

    @Test
    fun withAccountFilter_querySelectionUsesNonNullFields() = runTest {
        val account = AccountModel(
            name = null,
            type = "type",
            dataSet = "data_set",
        )
        subject.loadGroups(filters = listOf(GroupFilter.ByAccount(account))).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                "${ContactsContract.Groups.ACCOUNT_NAME} IS NULL AND " +
                    "${ContactsContract.Groups.ACCOUNT_TYPE}=? AND " +
                    "${ContactsContract.Groups.DATA_SET}=? AND " +
                    "${ContactsContract.Groups.DELETED}=0",
                arrayOf("type", "data_set"),
                any(),
            )
        }
    }

    @Test
    fun withAutoAddFilter_querySelection() = runTest {
        subject.loadGroups(filters = listOf(GroupFilter.AutoAdd(true))).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                "${ContactsContract.Groups.AUTO_ADD}=1 AND " +
                    "${ContactsContract.Groups.DELETED}=0",
                null,
                any(),
            )
        }
    }

    @Test
    fun withFavoriteFilter_querySelection() = runTest {
        subject.loadGroups(filters = listOf(GroupFilter.Favorites(false))).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                "${ContactsContract.Groups.FAVORITES}=0 AND " +
                    "${ContactsContract.Groups.DELETED}=0",
                null,
                any(),
            )
        }
    }

    @Test
    fun withSortUndefined_sortOrderIsNull() = runTest {
        subject.loadGroups(sort = GroupSort.UNDEFINED).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                any(),
                null,
                null,
            )
        }
    }

    @Test
    fun withSortByTitle_sortOrderIsTitleAsc() = runTest {
        subject.loadGroups(sort = GroupSort.BY_TITLE).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                any(),
                null,
                "${ContactsContract.Groups.TITLE} COLLATE LOCALIZED ASC",
            )
        }
    }

    @Test
    fun whenObserverTriggers_loadsDataAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        subject.loadGroups().test {
            awaitItem()
            observerSlot.captured.onChange(false)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 2) {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun loadGroup_queriesTheNonDeletedGroupById() = runTest {
        subject.loadGroup(GROUP_ID).first()

        verify {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                any(),
                "${ContactsContract.Groups._ID}=? AND ${ContactsContract.Groups.DELETED}=0",
                arrayOf(GROUP_ID.toString()),
                null,
            )
        }
    }

    @Test
    fun loadGroup_returnsTheGroup() = runTest {
        givenQueryRows(
            arrayOf(GROUP_ID, "Group Name", null, "Account Name", "Device", null, 1),
        )

        val result = subject.loadGroup(GROUP_ID).first()!!

        assertEquals(GROUP_ID, result.id)
        assertEquals("Group Name", result.title)
        assertTrue(result.isReadOnly)
    }

    @Test
    fun loadGroup_whenTheGroupIsMissing_returnsNull() = runTest {
        givenQueryRows()

        assertNull(subject.loadGroup(GROUP_ID).first())
    }

    @Test
    fun loadGroup_whenObserverTriggers_loadsTheGroupAgain() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        subject.loadGroup(GROUP_ID).test {
            awaitItem()
            observerSlot.captured.onChange(false)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 2) {
            contentResolver.query(ContactsContract.Groups.CONTENT_URI, any(), any(), any(), any())
        }
    }

    @Test
    fun deleteGroup_capturesTheUndoDataBeforeDeleting() = runTest {
        val undoData = Bundle()
        every { groupsDao.captureDeletionUndoData(GROUP_URI) } returns undoData
        every { groupsDao.delete(GROUP_URI) } returns 1

        val result = subject.deleteGroup(GROUP_ID)

        assertEquals(GroupDeletion(undoData = undoData), result)
        verifyOrder {
            groupsDao.captureDeletionUndoData(GROUP_URI)
            groupsDao.delete(GROUP_URI)
        }
    }

    @Test
    fun deleteGroup_whenNothingWasDeleted_returnsNull() = runTest {
        every { groupsDao.captureDeletionUndoData(GROUP_URI) } returns Bundle()
        every { groupsDao.delete(GROUP_URI) } returns 0

        assertNull(subject.deleteGroup(GROUP_ID))
    }

    @Test
    fun restoreGroup_undoesTheDeletion() = runTest {
        val undoData = Bundle()
        every { groupsDao.undoDeletion(undoData) } returns GROUP_URI

        val result = subject.restoreGroup(GroupDeletion(undoData = undoData))

        assertTrue(result)
    }

    @Test
    fun restoreGroup_whenTheGroupIsNotInserted_returnsFalse() = runTest {
        val undoData = Bundle()
        every { groupsDao.undoDeletion(undoData) } returns null

        val result = subject.restoreGroup(GroupDeletion(undoData = undoData))

        assertFalse(result)
    }

    private fun givenRegisteredContentObserver(): CapturingSlot<ContentObserver> {
        val observerSlot = slot<ContentObserver>()
        every {
            contentResolver.registerContentObserver(
                any(),
                true,
                capture(observerSlot),
            )
        } returns Unit
        return observerSlot
    }

    private fun givenQueryRows(vararg rows: Array<Any?>) {
        val projectionSlot = slot<Array<String>>()
        every {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                capture(projectionSlot),
                any(),
                any(),
                any(),
            )
        } answers {
            MatrixCursor(projectionSlot.captured).apply {
                rows.forEach { addRow(it) }
            }
        }
    }

    private companion object {
        const val GROUP_ID = 42L
        val GROUP_URI: Uri =
            ContentUris.withAppendedId(ContactsContract.Groups.CONTENT_URI, GROUP_ID)
    }
}
