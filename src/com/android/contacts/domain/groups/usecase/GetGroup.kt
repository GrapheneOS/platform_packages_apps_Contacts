package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.model.GroupDetails
import com.android.contacts.model.AccountTypeManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal fun interface GetGroup {
    operator fun invoke(groupId: Long): Flow<GroupDetails?>
}

internal class GetGroupImpl @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val groupMapper: GroupMapper,
    private val accountTypeManager: AccountTypeManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetGroup {

    override fun invoke(groupId: Long): Flow<GroupDetails?> {
        return groupsRepository.loadGroup(groupId)
            .map { column ->
                column
                    ?.let(groupMapper::map)
                    ?.let { group ->
                        GroupDetails(
                            group = group,
                            isMembershipEditable = isMembershipEditable(group),
                        )
                    }
            }
            .flowOn(ioDispatcher)
    }

    private fun isMembershipEditable(group: Group): Boolean {
        return accountTypeManager
            .getAccountType(group.account.type, group.account.dataSet)
            ?.isGroupMembershipEditable == true
    }
}
