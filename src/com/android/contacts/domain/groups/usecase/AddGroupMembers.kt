package com.android.contacts.domain.groups.usecase

import android.app.Activity
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.group.GroupUtil
import javax.inject.Inject

internal fun interface AddGroupMembers {
    suspend operator fun invoke(
        group: Group,
        contactIds: Set<Long>,
        callbackActivity: Class<out Activity>,
    ): Boolean
}

internal class AddGroupMembersImpl @Inject constructor(
    private val groupMembersRepository: GroupMembersRepository,
) : AddGroupMembers {

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
            rawContactIdsToAdd = rawContactIds,
            rawContactIdsToRemove = emptyList(),
            callbackActivity = callbackActivity,
            callbackAction = GroupUtil.ACTION_ADD_TO_GROUP,
        )
        return true
    }
}
