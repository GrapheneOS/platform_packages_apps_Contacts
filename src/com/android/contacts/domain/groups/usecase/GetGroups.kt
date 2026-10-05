package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.model.Group
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun interface GetGroups {
    operator fun invoke(account: AccountModel?): Flow<List<Group>?>
}

internal class GetGroupsImpl @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val groupMapper: GroupMapper,
) : GetGroups {
    override fun invoke(account: AccountModel?): Flow<List<Group>?> {
        return groupsRepository.loadGroups(account)
            .map { columns ->
                columns
                    ?.map(groupMapper::map)
                    ?.filter { !it.isDeleted && !it.isEmptyFFCGroup }
            }
    }
}
