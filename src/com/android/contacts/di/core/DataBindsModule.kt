package com.android.contacts.di.core

import com.android.contacts.data.intents.GetIntentLabel
import com.android.contacts.data.intents.GetIntentLabelImpl
import com.android.contacts.data.intents.IsIntentRegistered
import com.android.contacts.data.intents.IsIntentRegisteredImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataBindsModule {

    @Binds
    @Reusable
    abstract fun bindIsIntentRegistered(
        impl: IsIntentRegisteredImpl,
    ): IsIntentRegistered

    @Binds
    @Reusable
    abstract fun bindGetIntentLabel(
        impl: GetIntentLabelImpl,
    ): GetIntentLabel
}
