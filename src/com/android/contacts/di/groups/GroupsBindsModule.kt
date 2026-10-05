package com.android.contacts.di.groups

import com.android.contacts.data.groups.delegate.LoadGroupsRepositoryDelegate
import com.android.contacts.data.groups.delegate.LoadGroupsRepositoryDelegateImpl
import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.data.groups.repository.GroupsRepositoryImpl
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.mapper.GroupMapperImpl
import com.android.contacts.domain.groups.usecase.CreateOrEditGroupName
import com.android.contacts.domain.groups.usecase.CreateOrEditGroupNameImpl
import com.android.contacts.domain.groups.usecase.GetGroupNameMaxLenght
import com.android.contacts.domain.groups.usecase.GetGroupNameMaxLenghtImpl
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.domain.groups.usecase.GetGroupsImpl
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapper
import com.android.contacts.ui.group.list.screen.mapper.GroupsUiMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GroupsBindsModule {

    @Binds
    @Singleton
    abstract fun bindGroupsRepository(
        impl: GroupsRepositoryImpl,
    ): GroupsRepository

    @Binds
    @Reusable
    abstract fun bindLoadGroupsRepositoryDelegate(
        impl: LoadGroupsRepositoryDelegateImpl,
    ): LoadGroupsRepositoryDelegate

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
    abstract fun bindGetGroupNameMaxLenght(
        impl: GetGroupNameMaxLenghtImpl,
    ): GetGroupNameMaxLenght

    @Binds
    @Reusable
    abstract fun bindGetGroups(
        impl: GetGroupsImpl,
    ): GetGroups

    @Binds
    @Reusable
    abstract fun bindCreateOrEditGroupName(
        impl: CreateOrEditGroupNameImpl,
    ): CreateOrEditGroupName
}
