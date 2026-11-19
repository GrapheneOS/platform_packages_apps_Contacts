package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.model.GroupFilter
import com.android.contacts.domain.groups.model.GroupSort
import com.android.contacts.group.GroupUtil
import com.android.contacts.model.account.GoogleAccountType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal interface GetGroups {
    operator fun invoke(
        filters: List<GroupFilter> = emptyList(),
        sort: GroupSort = GroupSort.UNDEFINED,
    ): Flow<List<Group>?>
}

internal class GetGroupsImpl @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val groupMembersRepository: GroupMembersRepository,
    private val groupMapper: GroupMapper,
) : GetGroups {

    override fun invoke(
        filters: List<GroupFilter>,
        sort: GroupSort,
    ): Flow<List<Group>?> {
        return groupsRepository.loadGroups(filters, sort)
            .map { columns ->
                columns
                    ?.map(groupMapper::map)
                    ?.withMembersCounts()
                    ?.filterNot { isEmptyFFCGroup(it) }
            }
    }

    private suspend fun List<Group>.withMembersCounts(): List<Group> {
        val membersCounts = groupMembersRepository.getMembersCounts(map { it.id }) ?: return this
        return map { group -> group.copy(membersCount = membersCounts[group.id] ?: 0) }
    }

    private fun isEmptyFFCGroup(group: Group): Boolean {
        return GoogleAccountType.ACCOUNT_TYPE == group.account.type &&
            group.isReadOnly &&
            group.membersCount == 0 &&
            GroupUtil.isSystemIdFFC(group.systemId)
    }
}
