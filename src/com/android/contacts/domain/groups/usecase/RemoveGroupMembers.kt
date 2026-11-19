package com.android.contacts.domain.groups.usecase

import android.app.Activity
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.group.GroupUtil
import javax.inject.Inject

internal fun interface RemoveGroupMembers {
    suspend operator fun invoke(
        group: Group,
        contactIds: Set<Long>,
        callbackActivity: Class<out Activity>,
    ): Boolean
}

internal class RemoveGroupMembersImpl @Inject constructor(
    private val groupMembersRepository: GroupMembersRepository,
) : RemoveGroupMembers {

    override suspend fun invoke(
        group: Group,
        contactIds: Set<Long>,
        callbackActivity: Class<out Activity>,
    ): Boolean {
        val rawContactIds = groupMembersRepository.getRawContactIds(
            contactIds = contactIds,
            account = group.account,
        )
        if (rawContactIds.isEmpty()) return false

        groupMembersRepository.updateGroupMembers(
            groupId = group.id,
            rawContactIdsToAdd = emptyList(),
            rawContactIdsToRemove = rawContactIds,
            callbackActivity = callbackActivity,
            callbackAction = GroupUtil.ACTION_REMOVE_FROM_GROUP,
        )
        return true
    }
}
