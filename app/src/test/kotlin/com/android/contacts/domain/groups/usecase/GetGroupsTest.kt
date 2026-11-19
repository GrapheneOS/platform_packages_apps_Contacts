package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.model.GroupColumn
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.group.GroupUtil
import com.android.contacts.model.account.GoogleAccountType
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupColumnFactory
import com.android.contacts.tests.factory.GroupFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class GetGroupsTest {

    private val groupsRepository = mockk<GroupsRepository>()
    private val groupMembersRepository = mockk<GroupMembersRepository>()
    private val groupMapper = mockk<GroupMapper>()
    private val subject: GetGroups = GetGroupsImpl(
        groupsRepository = groupsRepository,
        groupMembersRepository = groupMembersRepository,
        groupMapper = groupMapper,
    )

    @Before
    fun setUp() {
        mockkStatic(GroupUtil::class)
        coEvery { groupMembersRepository.getMembersCounts(any()) } returns emptyMap()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun mapsGroups() = runTest {
        val groupColumn = GroupColumnFactory.build()
        val group = GroupFactory.build()
        every { groupsRepository.loadGroups(any()) } returns flowOf(listOf(groupColumn))
        every { groupMapper.map(any()) } returns group
        val account = AccountModelFactory.build()
        val filters = listOf(GroupFilter.ByAccount(account))

        val result = subject(filters).first()!!

        assertEquals(1, result.size)
        assertEquals(group.copy(membersCount = 0), result.first())
        verify { groupsRepository.loadGroups(filters) }
        verify { groupMapper.map(groupColumn) }
    }

    @Test
    fun doesNotReturnEmptyFFCGroups() = runTest {
        val groupColumn = GroupColumnFactory.build()
        val group = GroupFactory.build(
            account = AccountModelFactory.build(type = GoogleAccountType.ACCOUNT_TYPE),
            isReadOnly = true,
        )
        every { GroupUtil.isSystemIdFFC(any()) } returns true
        every { groupsRepository.loadGroups(any()) } returns flowOf(listOf(groupColumn))
        every { groupMapper.map(any()) } returns group
        val account = AccountModelFactory.build()

        val result = subject(listOf(GroupFilter.ByAccount(account))).first()

        assertTrue(result!!.isEmpty())
    }

    @Test
    fun addsTheMembersCounts() = runTest {
        val first = GroupFactory.build(id = 1L)
        val second = GroupFactory.build(id = 2L)
        every { groupsRepository.loadGroups(any()) } returns flowOf(
            listOf(GroupColumnFactory.build(id = 1L), GroupColumnFactory.build(id = 2L)),
        )
        every { groupMapper.map(any()) } answers {
            if (firstArg<GroupColumn>().id == 1L) first else second
        }
        coEvery { groupMembersRepository.getMembersCounts(listOf(1L, 2L)) } returns mapOf(1L to 5)

        val result = subject(emptyList()).first()

        assertEquals(listOf(first.copy(membersCount = 5), second.copy(membersCount = 0)), result)
    }

    @Test
    fun whenTheMembersCountsCannotBeLoaded_leavesThemUnknown() = runTest {
        val group = GroupFactory.build()
        every { groupsRepository.loadGroups(any()) } returns
            flowOf(listOf(GroupColumnFactory.build()))
        every { groupMapper.map(any()) } returns group
        coEvery { groupMembersRepository.getMembersCounts(any()) } returns null

        val result = subject(emptyList()).first()

        assertEquals(listOf(group), result)
        assertNull(result!!.single().membersCount)
    }

    @Test
    fun returnsFFCGroupsWithMembers() = runTest {
        val group = GroupFactory.build(
            account = AccountModelFactory.build(type = GoogleAccountType.ACCOUNT_TYPE),
            isReadOnly = true,
        )
        every { GroupUtil.isSystemIdFFC(any()) } returns true
        every { groupsRepository.loadGroups(any()) } returns
            flowOf(listOf(GroupColumnFactory.build()))
        every { groupMapper.map(any()) } returns group
        coEvery { groupMembersRepository.getMembersCounts(any()) } returns mapOf(group.id to 3)

        val result = subject(emptyList()).first()

        assertEquals(listOf(group.copy(membersCount = 3)), result)
    }

    @Test
    fun whenTheGroupsCannotBeLoaded_returnsNull() = runTest {
        every { groupsRepository.loadGroups(any()) } returns flowOf(null)

        assertNull(subject(emptyList()).first())
    }
}
