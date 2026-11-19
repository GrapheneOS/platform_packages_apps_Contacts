package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.GroupEvents
import javax.inject.Inject

internal fun interface DeleteGroup {
    suspend operator fun invoke(groupId: Long)
}

internal class DeleteGroupImpl @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val groupEvents: GroupEvents,
) : DeleteGroup {

    override suspend fun invoke(groupId: Long) {
        val deletion = groupsRepository.deleteGroup(groupId) ?: return
        groupEvents.notifyDeleted(deletion)
    }
}
