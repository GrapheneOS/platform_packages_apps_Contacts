package com.android.contacts.di.util

import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.domain.util.BuildBroadcastReceiverFlowImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainUtilBindsModule {

    @Binds
    @Reusable
    abstract fun bindBuildBroadcastReceiverFlow(
        impl: BuildBroadcastReceiverFlowImpl,
    ): BuildBroadcastReceiverFlow
}
