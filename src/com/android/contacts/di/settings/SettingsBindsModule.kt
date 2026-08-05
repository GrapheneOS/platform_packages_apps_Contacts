package com.android.contacts.di.settings

import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.DisplaySettingsRepositoryImpl
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
}
