package com.android.contacts.di.groups

import com.android.contacts.data.groups.repository.GroupMembersRepository
import com.android.contacts.data.groups.repository.GroupMembersRepositoryImpl
import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.data.groups.repository.GroupsRepositoryImpl
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.mapper.GroupMapperImpl
import com.android.contacts.domain.groups.usecase.AddGroupMembers
import com.android.contacts.domain.groups.usecase.AddGroupMembersImpl
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExists
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExistsImpl
import com.android.contacts.domain.groups.usecase.DeleteGroup
import com.android.contacts.domain.groups.usecase.DeleteGroupImpl
import com.android.contacts.domain.groups.usecase.GetGroup
import com.android.contacts.domain.groups.usecase.GetGroupImpl
import com.android.contacts.domain.groups.usecase.GetGroupMembers
import com.android.contacts.domain.groups.usecase.GetGroupMembersImpl
import com.android.contacts.domain.groups.usecase.GetGroupSendTargets
import com.android.contacts.domain.groups.usecase.GetGroupSendTargetsImpl
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.domain.groups.usecase.GetGroupsImpl
import com.android.contacts.domain.groups.usecase.RemoveGroupMembers
import com.android.contacts.domain.groups.usecase.RemoveGroupMembersImpl
import com.android.contacts.ui.group.detail.screen.mapper.GroupUiStateMapper
import com.android.contacts.ui.group.detail.screen.mapper.GroupUiStateMapperImpl
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapper
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GroupsBindsModule {

    @Binds
    @Reusable
    abstract fun bindGroupsRepository(
        impl: GroupsRepositoryImpl,
    ): GroupsRepository

    @Binds
    @Reusable
    abstract fun bindGroupMapper(
        impl: GroupMapperImpl,
    ): GroupMapper

    @Binds
    @Reusable
    abstract fun bindGroupsUiMapper(
        impl: GroupsUiMapperImpl,
    ): GroupsUiMapper

    @Binds
    @Reusable
    abstract fun bindGetGroups(
        impl: GetGroupsImpl,
    ): GetGroups

    @Binds
    @Reusable
    abstract fun bindCheckIfGroupNameExists(
        impl: CheckIfGroupNameExistsImpl,
    ): CheckIfGroupNameExists

    @Binds
    @Reusable
    abstract fun bindGroupMembersRepository(
        impl: GroupMembersRepositoryImpl,
    ): GroupMembersRepository

    @Binds
    @Reusable
    abstract fun bindGetGroup(
        impl: GetGroupImpl,
    ): GetGroup

    @Binds
    @Reusable
    abstract fun bindGetGroupMembers(
        impl: GetGroupMembersImpl,
    ): GetGroupMembers

    @Binds
    @Reusable
    abstract fun bindGetGroupSendTargets(
        impl: GetGroupSendTargetsImpl,
    ): GetGroupSendTargets

    @Binds
    @Reusable
    abstract fun bindAddGroupMembers(
        impl: AddGroupMembersImpl,
    ): AddGroupMembers

    @Binds
    @Reusable
    abstract fun bindRemoveGroupMembers(
        impl: RemoveGroupMembersImpl,
    ): RemoveGroupMembers

    @Binds
    @Reusable
    abstract fun bindDeleteGroup(
        impl: DeleteGroupImpl,
    ): DeleteGroup

    @Binds
    @Reusable
    abstract fun bindGroupUiStateMapper(
        impl: GroupUiStateMapperImpl,
    ): GroupUiStateMapper
}
