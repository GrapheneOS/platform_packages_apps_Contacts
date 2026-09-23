package com.android.contacts.data.groups.repository

import com.android.contacts.data.groups.delegate.LoadGroupsRepositoryDelegate
import com.android.contacts.data.groups.delegate.LoadGroupsRepositoryDelegateImpl
import javax.inject.Inject

internal interface GroupsRepository : LoadGroupsRepositoryDelegate

internal class GroupsRepositoryImpl @Inject constructor(
    private val loadGroupsRepositoryDelegateImpl: LoadGroupsRepositoryDelegateImpl,
) : GroupsRepository,
    LoadGroupsRepositoryDelegate by loadGroupsRepositoryDelegateImpl
