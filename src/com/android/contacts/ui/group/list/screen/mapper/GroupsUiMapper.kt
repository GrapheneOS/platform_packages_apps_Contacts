package com.android.contacts.ui.group.list.screen.mapper

import android.content.res.Resources
import com.android.contacts.R
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.ui.group.list.screen.model.AccountGroupsItem
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface GroupsUiMapper {
    fun map(groups: List<Group>): ImmutableList<AccountGroupsItem>
    fun map(account: AccountDisplayModel?, groups: List<Group> = emptyList()): AccountGroupsItem
}

internal class GroupsUiMapperImpl @Inject constructor(
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    private val resources: Resources,
) : GroupsUiMapper {
    override fun map(
        groups: List<Group>,
    ): ImmutableList<AccountGroupsItem> {
        val groupsMap = groups.groupBy {
            it.account?.let(accountDisplayModelMapper::map)
        }

        return groupsMap
            .map { (account, groups) -> map(account, groups) }
            .toImmutableList()
    }

    override fun map(
        account: AccountDisplayModel?,
        groups: List<Group>,
    ): AccountGroupsItem {
        return AccountGroupsItem(
            account = account?.account,
            accountName = account?.name ?: resources.getString(R.string.group_without_account),
            groups = groups.map(::mapItem).toImmutableList(),
            canCreateGroup = account?.areContactsWritable != false,
        )
    }

    private fun mapItem(group: Group): GroupUiItem {
        return GroupUiItem(
            id = group.id,
            name = group.name,
            summaryCount = group.summaryCount,
        )
    }
}
