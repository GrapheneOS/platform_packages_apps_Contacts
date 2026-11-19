package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.groups.model.GroupSendTargetColumn
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.domain.groups.model.GroupSendTargets
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GetGroupSendTargetsTest {

    private val groupMembersRepository = mockk<GroupMembersRepository>()
    private val subject: GetGroupSendTargets = GetGroupSendTargetsImpl(
        groupMembersRepository = groupMembersRepository,
    )

    @Test
    fun queriesTheRequestedKind() = runTest {
        givenTargets()

        subject(setOf(1L, 2L), ContactDataKind.Email)

        coVerify { groupMembersRepository.getSendTargets(setOf(1L, 2L), ContactDataKind.Email) }
    }

    @Test
    fun whenNoContactHasValues_returnsNone() = runTest {
        givenTargets()

        val result = subject(setOf(1L, 2L), KIND)

        assertEquals(GroupSendTargets.None, result)
    }

    @Test
    fun whenEveryContactHasASingleValue_returnsDirect() = runTest {
        givenTargets(
            target(contactId = 1L, dataId = 10L, value = "111"),
            target(contactId = 2L, dataId = 20L, value = "222"),
        )

        val result = subject(setOf(1L, 2L), KIND)

        assertEquals(
            GroupSendTargets.Direct(values = listOf("111", "222"), hasContactsWithoutValue = false),
            result,
        )
    }

    @Test
    fun whenAContactHasSeveralValuesAndADefault_usesTheDefault() = runTest {
        givenTargets(
            target(contactId = 1L, dataId = 10L, value = "111"),
            target(contactId = 1L, dataId = 11L, value = "112", isSuperPrimary = true),
            target(contactId = 2L, dataId = 20L, value = "222"),
        )

        val result = subject(setOf(1L, 2L), KIND)

        assertEquals(
            GroupSendTargets.Direct(values = listOf("112", "222"), hasContactsWithoutValue = false),
            result,
        )
    }

    @Test
    fun whenSomeContactsHaveNoValue_flagsThemAsLeftOut() = runTest {
        givenTargets(target(contactId = 1L, dataId = 10L, value = "111"))

        val result = subject(setOf(1L, 2L), KIND)

        assertEquals(
            GroupSendTargets.Direct(values = listOf("111"), hasContactsWithoutValue = true),
            result,
        )
    }

    @Test
    fun whenAContactHasSeveralValuesWithoutDefault_needsPicker() = runTest {
        givenTargets(
            target(contactId = 1L, dataId = 10L, value = "111"),
            target(contactId = 1L, dataId = 11L, value = "112"),
            target(contactId = 2L, dataId = 20L, value = "221"),
            target(contactId = 2L, dataId = 21L, value = "222", isSuperPrimary = true),
            target(contactId = 3L, dataId = 30L, value = "333"),
        )

        val result = subject(setOf(1L, 2L, 3L, 4L), KIND)

        assertEquals(
            GroupSendTargets.NeedsPicker(
                contactIds = setOf(1L, 2L, 3L, 4L),
                defaultSelection = setOf(10L, 21L, 30L),
            ),
            result,
        )
    }

    private fun givenTargets(vararg targets: GroupSendTargetColumn) {
        coEvery { groupMembersRepository.getSendTargets(any(), any()) } returns targets.toList()
    }

    private fun target(
        contactId: Long,
        dataId: Long,
        value: String,
        isSuperPrimary: Boolean = false,
    ) = GroupSendTargetColumn(
        contactId = contactId,
        dataId = dataId,
        value = value,
        isSuperPrimary = isSuperPrimary,
    )

    private companion object {
        val KIND = ContactDataKind.Phone()
    }
}
