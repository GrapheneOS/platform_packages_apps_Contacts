package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.GroupDetails
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountType
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupColumnFactory
import com.android.contacts.tests.factory.GroupFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetGroupTest {

    private val groupsRepository = mockk<GroupsRepository>()
    private val groupMapper = mockk<GroupMapper>()
    private val accountTypeManager = mockk<AccountTypeManager>()
    private val subject: GetGroup = GetGroupImpl(
        groupsRepository = groupsRepository,
        groupMapper = groupMapper,
        accountTypeManager = accountTypeManager,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private val groupColumn = GroupColumnFactory.build(id = 7L)
    private val group = GroupFactory.build(
        id = 7L,
        account = AccountModelFactory.build(type = "com.example", dataSet = "plus"),
    )

    @Test
    fun mapsTheGroup() = runTest {
        every { groupsRepository.loadGroup(7L) } returns flowOf(groupColumn)
        every { groupMapper.map(groupColumn) } returns group
        givenAccountType(isGroupMembershipEditable = true)

        val result = subject(7L).first()

        assertEquals(GroupDetails(group = group, isMembershipEditable = true), result)
        verify { accountTypeManager.getAccountType("com.example", "plus") }
    }

    @Test
    fun whenTheGroupDoesNotExist_emitsNull() = runTest {
        every { groupsRepository.loadGroup(7L) } returns flowOf(null)

        assertNull(subject(7L).first())
    }

    @Test
    fun whenTheAccountTypeDoesNotAllowMembershipEdits_membershipIsNotEditable() = runTest {
        every { groupsRepository.loadGroup(7L) } returns flowOf(groupColumn)
        every { groupMapper.map(groupColumn) } returns group
        givenAccountType(isGroupMembershipEditable = false)

        val result = subject(7L).first()!!

        assertFalse(result.isMembershipEditable)
    }

    @Test
    fun whenTheAccountTypeIsUnknown_membershipIsNotEditable() = runTest {
        every { groupsRepository.loadGroup(7L) } returns flowOf(groupColumn)
        every { groupMapper.map(groupColumn) } returns group
        every { accountTypeManager.getAccountType(any<String>(), any()) } returns null

        val result = subject(7L).first()!!

        assertFalse(result.isMembershipEditable)
    }

    private fun givenAccountType(isGroupMembershipEditable: Boolean) {
        val accountType = mockk<AccountType> {
            every { this@mockk.isGroupMembershipEditable } returns isGroupMembershipEditable
        }
        every { accountTypeManager.getAccountType(any<String>(), any()) } returns accountType
    }
}
