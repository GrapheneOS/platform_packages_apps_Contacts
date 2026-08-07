package com.android.contacts.di.settings

import com.android.contacts.data.accounts.repository.AccountsRepository
import com.android.contacts.data.accounts.repository.AccountsRepositoryImpl
import com.android.contacts.data.appinfo.repository.AppInfoRepository
import com.android.contacts.data.appinfo.repository.AppInfoRepositoryImpl
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepository
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepositoryImpl
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.data.permissions.repository.PermissionsRepositoryImpl
import com.android.contacts.data.profile.repository.ProfileRepository
import com.android.contacts.data.profile.repository.ProfileRepositoryImpl
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.DisplaySettingsRepositoryImpl
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepositoryImpl
import com.android.contacts.data.simimport.repository.SimImportResultRepository
import com.android.contacts.data.simimport.repository.SimImportResultRepositoryImpl
import com.android.contacts.domain.settings.usecase.GetSettingsData
import com.android.contacts.domain.settings.usecase.GetSettingsDataImpl
import com.android.contacts.ui.settings.screen.mapper.SettingsUiStateMapper
import com.android.contacts.ui.settings.screen.mapper.SettingsUiStateMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SettingsBindsModule {

    @Binds
    @Reusable
    abstract fun bindDisplaySettingsRepository(
        impl: DisplaySettingsRepositoryImpl,
    ): DisplaySettingsRepository

    @Binds
    @Reusable
    abstract fun bindSettingsAvailabilityRepository(
        impl: SettingsAvailabilityRepositoryImpl,
    ): SettingsAvailabilityRepository

    @Binds
    @Reusable
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl,
    ): ProfileRepository

    @Binds
    @Reusable
    abstract fun bindContactsFilterRepository(
        impl: ContactsFilterRepositoryImpl,
    ): ContactsFilterRepository

    @Binds
    @Reusable
    abstract fun bindSimImportResultRepository(
        impl: SimImportResultRepositoryImpl,
    ): SimImportResultRepository

    @Binds
    @Reusable
    abstract fun bindAppInfoRepository(
        impl: AppInfoRepositoryImpl,
    ): AppInfoRepository

    @Binds
    @Reusable
    abstract fun bindAccountsRepository(
        impl: AccountsRepositoryImpl,
    ): AccountsRepository

    @Binds
    @Reusable
    abstract fun bindPermissionsRepository(
        impl: PermissionsRepositoryImpl,
    ): PermissionsRepository

    @Binds
    @Reusable
    abstract fun bindGetSettingsData(
        impl: GetSettingsDataImpl,
    ): GetSettingsData

    @Binds
    @Reusable
    abstract fun bindSettingsUiStateMapper(
        impl: SettingsUiStateMapperImpl,
    ): SettingsUiStateMapper
}
