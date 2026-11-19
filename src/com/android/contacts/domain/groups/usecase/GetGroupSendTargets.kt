package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.groups.model.GroupSendTargetColumn
import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.domain.groups.model.GroupSendTargets
import javax.inject.Inject

internal fun interface GetGroupSendTargets {
    suspend operator fun invoke(
        contactIds: Set<Long>,
        kind: ContactDataKind,
    ): GroupSendTargets
}

internal class GetGroupSendTargetsImpl @Inject constructor(
    private val groupMembersRepository: GroupMembersRepository,
) : GetGroupSendTargets {

    override suspend fun invoke(
        contactIds: Set<Long>,
        kind: ContactDataKind,
    ): GroupSendTargets {
        val targetsByContact = groupMembersRepository.getSendTargets(contactIds, kind)
            .groupBy { it.contactId }
        if (targetsByContact.isEmpty()) return GroupSendTargets.None

        val defaults = targetsByContact.values.mapNotNull { it.defaultTarget() }
        return when {
            defaults.size < targetsByContact.size -> {
                GroupSendTargets.NeedsPicker(
                    contactIds = contactIds,
                    defaultSelection = targetsByContact.values
                        .mapTo(mutableSetOf()) { targets -> targets.preselectedTarget().dataId },
                )
            }
            else -> {
                GroupSendTargets.Direct(
                    values = defaults.map { it.value },
                    hasContactsWithoutValue = targetsByContact.size < contactIds.size,
                )
            }
        }
    }

    private fun List<GroupSendTargetColumn>.defaultTarget(): GroupSendTargetColumn? {
        return firstOrNull { it.isSuperPrimary } ?: singleOrNull()
    }

    private fun List<GroupSendTargetColumn>.preselectedTarget(): GroupSendTargetColumn {
        return firstOrNull { it.isSuperPrimary } ?: first()
    }
}
