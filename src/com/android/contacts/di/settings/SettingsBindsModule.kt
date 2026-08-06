package com.android.contacts.di.settings

import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepository
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepositoryImpl
import com.android.contacts.data.profile.repository.ProfileRepository
import com.android.contacts.data.profile.repository.ProfileRepositoryImpl
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.DisplaySettingsRepositoryImpl
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepositoryImpl
import com.android.contacts.data.simimport.repository.SimImportResultRepository
import com.android.contacts.data.simimport.repository.SimImportResultRepositoryImpl
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
}
