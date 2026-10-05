package com.android.contacts.data.groups.delegate

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class LoadRawContactsRepositoryDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject: LoadGroupsRepositoryDelegate = LoadGroupsRepositoryDelegateImpl(
        contentResolver = contentResolver,
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
                20,
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
            assertEquals(20, summaryCount)
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                any(),
                any(),
                any(),
                any(),
            )
        }
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
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
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
}
