package com.android.contacts.domain.groups.usecase

import android.app.Activity
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.GroupFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class RemoveGroupMembersTest {

    private val groupMembersRepository = mockk<GroupMembersRepository>(relaxed = true)
    private val subject: RemoveGroupMembers = RemoveGroupMembersImpl(
        groupMembersRepository = groupMembersRepository,
    )

    @Test
    fun removesTheRawContactsOfTheGroupAccount() = runTest {
        val account = AccountModelFactory.build(type = "com.example")
        val group = GroupFactory.build(id = 7L, account = account)
        coEvery { groupMembersRepository.getRawContactIds(any(), any()) } returns listOf(10L, 11L)

        val result = subject(group, setOf(1L, 2L), Activity::class.java)

        assertTrue(result)
        coVerify { groupMembersRepository.getRawContactIds(setOf(1L, 2L), account) }
        coVerify {
            groupMembersRepository.updateGroupMembers(
                groupId = 7L,
                rawContactIdsToAdd = emptyList(),
                rawContactIdsToRemove = listOf(10L, 11L),
                callbackActivity = Activity::class.java,
                callbackAction = GroupUtil.ACTION_REMOVE_FROM_GROUP,
            )
        }
    }

    @Test
    fun whenNoRawContactsInTheGroupAccount_doesNotUpdateTheGroup() = runTest {
        coEvery { groupMembersRepository.getRawContactIds(any(), any()) } returns emptyList()

        val result = subject(GroupFactory.build(), setOf(1L), Activity::class.java)

        assertFalse(result)
        coVerify(exactly = 0) {
            groupMembersRepository.updateGroupMembers(any(), any(), any(), any(), any())
        }
    }
}
