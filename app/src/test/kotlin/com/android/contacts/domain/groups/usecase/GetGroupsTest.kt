package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.group.GroupUtil
import com.android.contacts.model.account.GoogleAccountType
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupColumnFactory
import com.android.contacts.tests.factory.GroupFactory
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class GetGroupsTest {

    private val groupsRepository = mockk<GroupsRepository>()
    private val groupMapper = mockk<GroupMapper>()
    private val subject: GetGroups = GetGroupsImpl(
        groupsRepository = groupsRepository,
        groupMapper = groupMapper,
    )

    @Before
    fun setUp() {
        mockkStatic(GroupUtil::class)
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
        assertEquals(group, result.first())
        verify { groupsRepository.loadGroups(filters) }
        verify { groupMapper.map(groupColumn) }
    }

    @Test
    fun doesNotReturnEmptyFFCGroups() = runTest {
        val groupColumn = GroupColumnFactory.build()
        val group = GroupFactory.build(
            account = AccountModelFactory.build(type = GoogleAccountType.ACCOUNT_TYPE),
            isReadOnly = true,
            summaryCount = 0,
        )
        every { GroupUtil.isSystemIdFFC(any()) } returns true
        every { groupsRepository.loadGroups(any()) } returns flowOf(listOf(groupColumn))
        every { groupMapper.map(any()) } returns group
        val account = AccountModelFactory.build()

        val result = subject(listOf(GroupFilter.ByAccount(account))).first()

        assertTrue(result!!.isEmpty())
    }
}
