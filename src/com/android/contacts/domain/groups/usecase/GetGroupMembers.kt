package com.android.contacts.domain.groups.usecase

import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.di.core.DefaultDispatcher
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.model.ContactItem
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal fun interface GetGroupMembers {
    operator fun invoke(groupId: Long): Flow<List<ContactItem>>
}

internal class GetGroupMembersImpl @Inject constructor(
    private val groupMembersRepository: GroupMembersRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val contactItemMapper: ContactItemMapper,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : GetGroupMembers {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(groupId: Long): Flow<List<ContactItem>> {
        return displaySettingsRepository.observeDisplaySettings()
            .map { it.sortOrder to it.displayOrder }
            .distinctUntilChanged()
            .flatMapLatest { (sortOrder, displayOrder) ->
                groupMembersRepository.queryGroupMembers(groupId, sortOrder)
                    .map { columns ->
                        columns.map { contactItemMapper.map(it, displayOrder) }
                    }
            }
            .flowOn(defaultDispatcher)
    }
}
