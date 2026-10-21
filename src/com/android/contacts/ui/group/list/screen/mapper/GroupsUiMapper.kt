package com.android.contacts.ui.group.list.screen.mapper

import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.ui.group.list.screen.model.AccountGroupsItem
import com.android.contacts.ui.group.list.screen.model.GroupUiItem
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface GroupsUiMapper {
    fun map(
        groups: List<Group>,
        insertableAccounts: List<AccountDisplayModel>,
        defaultAccount: AccountModel?,
    ): ImmutableList<AccountGroupsItem>
}

internal class GroupsUiMapperImpl @Inject constructor(
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
) : GroupsUiMapper {

    override fun map(
        groups: List<Group>,
        insertableAccounts: List<AccountDisplayModel>,
        defaultAccount: AccountModel?,
    ): ImmutableList<AccountGroupsItem> {
        val groupsMap = groups.groupBy { accountDisplayModelMapper.map(it.account) }

        val defaultAccountDisplay = defaultAccount
            ?.let { insertableAccounts.find { it.account == defaultAccount } }

        // The default account should always be available to add a new group,
        // if there is a default account and it can create groups
        val groupsIncludeDefaultAccount = groupsMap.keys.any { it.account == defaultAccount }
        val groupsMapWithDefault = when {
            defaultAccountDisplay == null || groupsIncludeDefaultAccount -> {
                groupsMap
            }
            else -> {
                mapOf(defaultAccountDisplay to emptyList<Group>()) + groupsMap
            }
        }

        return groupsMapWithDefault
            .map { (account, groups) ->
                buildItem(
                    account = account,
                    groups = groups,
                    canCreateGroup = insertableAccounts.any { it.account == account.account },
                )
            }
            .toImmutableList()
    }

    private fun buildItem(
        account: AccountDisplayModel,
        groups: List<Group>,
        canCreateGroup: Boolean,
    ): AccountGroupsItem {
        return AccountGroupsItem(
            account = account.account,
            accountName = account.name ?: account.type ?: "",
            groups = groups.map(::mapItem).toImmutableList(),
            canCreateGroup = canCreateGroup,
        )
    }

    private fun mapItem(group: Group): GroupUiItem {
        return GroupUiItem(
            id = group.id,
            name = group.name.trim(),
            summaryCount = group.summaryCount,
        )
    }
}
