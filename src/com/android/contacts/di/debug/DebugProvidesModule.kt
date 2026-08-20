package com.android.contacts.di.debug

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class DebugProvidesModule {
    @Provides
    @Reusable
    @SeedTestContactsCount
    @Suppress("detekt:FunctionOnlyReturningConstant")
    fun providesSeedTestContactsCount(): Int {
        return 100
    }
}
