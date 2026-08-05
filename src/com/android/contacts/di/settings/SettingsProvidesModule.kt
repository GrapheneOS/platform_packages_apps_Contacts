package com.android.contacts.di.settings

import android.content.Context
import com.android.contacts.list.ProviderStatusWatcher
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class SettingsProvidesModule {

    @Provides
    @Reusable
    fun providerStatusWatcher(
        @ApplicationContext context: Context,
    ): ProviderStatusWatcher {
        return ProviderStatusWatcher.getInstance(context)
    }
}
