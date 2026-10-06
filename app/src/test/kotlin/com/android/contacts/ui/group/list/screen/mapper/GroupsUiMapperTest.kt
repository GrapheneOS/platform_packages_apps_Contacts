package com.android.contacts.ui.group.list.screen.mapper

import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupFactory
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GroupsUiMapperTest {

    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>()
    private val subject: GroupsUiMapper = GroupsUiMapperImpl(
        accountDisplayModelMapper = accountDisplayModelMapper,
    )

    @Test
    fun mapsGroupAndAccountFieldsCorrectly() {
        val account = AccountModelFactory.build(name = "Name")
        val accountDisplayModel = AccountDisplayModelFactory.build(
            account = account,
            name = "Display Name",
        )
        every { accountDisplayModelMapper.map(account) } returns accountDisplayModel
        val group = GroupFactory.build(
            id = 123L,
            name = "Group",
            summaryCount = 20,
            account = account,
        )

        val result = subject.map(listOf(group), emptyList(), null)

        assertEquals(1, result.size)
        val accountGroup = result.first()
        assertEquals(account, accountGroup.account)
        assertEquals(accountDisplayModel.name, accountGroup.accountName)
        assertEquals(
            persistentListOf(
                GroupUiItem(
                    id = 123L,
                    name = "Group",
                    summaryCount = 20,
                ),
            ),
            accountGroup.groups,
        )
    }

    @Test
    fun whenAccountIsInsertable_canCreateGroupIsTrue() {
        val account = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(account.account) } returns account
        val group = GroupFactory.build(account = account.account)

        val result = subject.map(listOf(group), listOf(account), null)

        assertTrue(result.first().canCreateGroup)
    }

    @Test
    fun whenAccountIsNotInsertable_canCreateGroupIsFalse() {
        val account = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(account.account) } returns account
        val group = GroupFactory.build(account = account.account)

        val result = subject.map(listOf(group), emptyList(), null)

        assertFalse(result.first().canCreateGroup)
    }

    @Test
    fun whenDefaultAccountIsIncluded_doNotAddIt() {
        val account = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(account.account) } returns account
        val group = GroupFactory.build(account = account.account)

        val result = subject.map(listOf(group), emptyList(), account.account)

        assertEquals(1, result.size)
        val accountGroup = result.first()
        assertEquals(account.account, accountGroup.account)
    }

    @Test
    fun whenDefaultAccountIsInsertableAndNotIncluded_addIt() {
        val defaultAccount = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(defaultAccount.account) } returns defaultAccount

        val result = subject.map(emptyList(), listOf(defaultAccount), defaultAccount.account)

        assertEquals(1, result.size)
        val accountGroup = result.first()
        assertEquals(defaultAccount.account, accountGroup.account)
        assertEquals(0, accountGroup.groups.size)
    }

    @Test
    fun whenDefaultAccountIsNotInsertable_doNotAddIt() {
        val defaultAccount = AccountDisplayModelFactory.build()
        every { accountDisplayModelMapper.map(defaultAccount.account) } returns defaultAccount

        val result = subject.map(emptyList(), emptyList(), defaultAccount.account)

        assertTrue(result.isEmpty())
    }
}
