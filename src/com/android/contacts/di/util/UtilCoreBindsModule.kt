package com.android.contacts.di.util

import com.android.contacts.util.core.BuildBroadcastReceiverFlow
import com.android.contacts.util.core.BuildBroadcastReceiverFlowImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UtilCoreBindsModule {

    @Binds
    @Reusable
    abstract fun bindBuildBroadcastReceiverFlow(
        impl: BuildBroadcastReceiverFlowImpl,
    ): BuildBroadcastReceiverFlow
}
