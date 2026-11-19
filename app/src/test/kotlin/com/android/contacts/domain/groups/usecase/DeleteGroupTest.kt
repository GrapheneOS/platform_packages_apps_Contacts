package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.GroupEvents
import com.android.contacts.domain.groups.model.GroupDeletion
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class DeleteGroupTest {

    private val groupsRepository = mockk<GroupsRepository>()
    private val groupEvents = mockk<GroupEvents>(relaxed = true)
    private val subject: DeleteGroup = DeleteGroupImpl(
        groupsRepository = groupsRepository,
        groupEvents = groupEvents,
    )

    @Test
    fun whenTheGroupIsDeleted_notifiesTheDeletion() = runTest {
        val deletion = mockk<GroupDeletion>()
        coEvery { groupsRepository.deleteGroup(7L) } returns deletion

        subject(7L)

        verify { groupEvents.notifyDeleted(deletion) }
    }

    @Test
    fun whenNothingIsDeleted_doesNotNotify() = runTest {
        coEvery { groupsRepository.deleteGroup(7L) } returns null

        subject(7L)

        verify(exactly = 0) { groupEvents.notifyDeleted(any()) }
    }
}
