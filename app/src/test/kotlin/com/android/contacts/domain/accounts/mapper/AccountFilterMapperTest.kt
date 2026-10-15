package com.android.contacts.domain.accounts.mapper

import android.content.Context
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.google.common.base.Predicate
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class AccountFilterMapperTest {

    private val context = mockk<Context>()
    private val subject: AccountFilterMapper = AccountFilterMapperImpl(context)

    @Before
    fun setUp() {
        mockkStatic(AccountTypeManager::class)
        mockkStatic(AccountTypeManager.AccountFilter::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun mapNull() {
        assertEquals(
            AccountTypeManager.AccountFilter.ALL,
            subject.map(null),
        )
    }

    @Test
    fun mapAll() {
        assertEquals(
            AccountTypeManager.AccountFilter.ALL,
            subject.map(AccountFilter.ALL),
        )
    }

    @Test
    fun mapContactsWritable() {
        assertEquals(
            AccountTypeManager.AccountFilter.CONTACTS_WRITABLE,
            subject.map(AccountFilter.CONTACTS_WRITABLE),
        )
    }

    @Test
    fun mapContactsInsertable() {
        val filterPredicate = mockk<Predicate<AccountInfo>>()
        every { AccountTypeManager.insertableFilter(context) } returns filterPredicate
        assertEquals(
            filterPredicate,
            subject.map(AccountFilter.CONTACTS_INSERTABLE),
        )
    }

    @Test
    fun mapDrawerDisplayable() {
        assertEquals(
            AccountTypeManager.AccountFilter.DRAWER_DISPLAYABLE,
            subject.map(AccountFilter.DRAWER_DISPLAYABLE),
        )
    }

    @Test
    fun mapGroupsWritable() {
        assertEquals(
            AccountTypeManager.AccountFilter.GROUPS_WRITABLE,
            subject.map(AccountFilter.GROUPS_WRITABLE),
        )
    }

    @Test
    fun mapGroupsInsertable() {
        val filterPredicate = mockk<Predicate<AccountInfo>>()
        every { AccountTypeManager.groupInsertableFilter(context) } returns filterPredicate
        assertEquals(
            filterPredicate,
            subject.map(AccountFilter.GROUPS_INSERTABLE),
        )
    }
}
