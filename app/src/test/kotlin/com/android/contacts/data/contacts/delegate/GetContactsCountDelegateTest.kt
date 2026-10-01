package com.android.contacts.data.contacts.delegate

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.MatrixCursor
import android.provider.ContactsContract
import app.cash.turbine.test
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.tests.factory.AccountModelFactory
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GetContactsCountDelegateTest {

    private val contentResolver = mockk<ContentResolver>(relaxed = true)

    private val subject: GetContactsCountDelegate = GetContactsCountDelegateImpl(
        contentResolver = contentResolver,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun whenEmptyRows_returnsEmptyMap() = runTest {
        givenQueryRows()

        val result = subject.getContactsCount().first()!!

        assertEquals(emptyMap<AccountModel, Int>(), result)
    }

    @Test
    fun withLinkedContacts_returnsSingleCount() = runTest {
        val account = AccountModelFactory.build()
        givenQueryRows(
            arrayOf(1L, account.name, account.type, account.dataSet),
            arrayOf(1L, account.name, account.type, account.dataSet),
        )

        val result = subject.getContactsCount().first()!!

        assertEquals(1, result[account])
    }

    @Test
    fun withMultipleContactsOnSameAccount_countThem() = runTest {
        val account = AccountModelFactory.build()
        givenQueryRows(
            arrayOf(1L, account.name, account.type, account.dataSet),
            arrayOf(2L, account.name, account.type, account.dataSet),
            arrayOf(3L, account.name, account.type, account.dataSet),
        )

        val result = subject.getContactsCount().first()!!

        assertEquals(3, result[account])
    }

    @Test
    fun withMultipleContactsOnMultipleAccounts_countThem() = runTest {
        val account1 = AccountModelFactory.build(name = "Account 1")
        val account2 = AccountModelFactory.build(name = "Account 2")
        givenQueryRows(
            arrayOf(1L, account1.name, account1.type, account1.dataSet),
            arrayOf(2L, account2.name, account2.type, account2.dataSet),
            arrayOf(3L, account2.name, account2.type, account2.dataSet),
        )

        val result = subject.getContactsCount().first()!!

        assertEquals(1, result[account1])
        assertEquals(2, result[account2])
    }

    @Test
    fun whenObserverTriggers_queryEverytimeWithRightArguments() = runTest {
        val observerSlot = givenRegisteredContentObserver()

        subject.getContactsCount().test {
            awaitItem()
            observerSlot.captured.onChange(false)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 2) {
            contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                GetContactsCountDelegateImpl.PROJECTION,
                null,
                null,
                null,
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
                any(),
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
