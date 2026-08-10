package com.android.contacts.di.intents

import com.android.contacts.data.intents.repository.IntentResolverRepository
import com.android.contacts.data.intents.repository.IntentResolverRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class IntentsBindsModule {

    @Binds
    @Reusable
    abstract fun bindIntentResolverRepository(
        impl: IntentResolverRepositoryImpl,
    ): IntentResolverRepository
}
