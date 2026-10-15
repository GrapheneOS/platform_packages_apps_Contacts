package com.android.contacts.di.groups

import com.android.contacts.data.groups.repository.GroupsRepository
import com.android.contacts.data.groups.repository.GroupsRepositoryImpl
import com.android.contacts.domain.groups.mapper.GroupMapper
import com.android.contacts.domain.groups.mapper.GroupMapperImpl
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExists
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExistsImpl
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.domain.groups.usecase.GetGroupsImpl
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
    abstract fun bindGetGroups(
        impl: GetGroupsImpl,
    ): GetGroups

    @Binds
    @Reusable
    abstract fun bindCheckIfGroupNameExists(
        impl: CheckIfGroupNameExistsImpl,
    ): CheckIfGroupNameExists
}
