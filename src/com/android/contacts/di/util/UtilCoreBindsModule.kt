package com.android.contacts.di.util

import com.android.contacts.util.core.AcquireWakeLock
import com.android.contacts.util.core.AcquireWakeLockImpl
import com.android.contacts.util.core.BuildBroadcastReceiverFlow
import com.android.contacts.util.core.BuildBroadcastReceiverFlowImpl
import com.android.contacts.util.core.IsPermissionGranted
import com.android.contacts.util.core.IsPermissionGrantedImpl
import com.android.contacts.util.core.SaveUriToFile
import com.android.contacts.util.core.SaveUriToFileImpl
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
    abstract fun bindAcquiteWakeLock(
        impl: AcquireWakeLockImpl,
    ): AcquireWakeLock

    @Binds
    @Reusable
    abstract fun bindBuildBroadcastReceiverFlow(
        impl: BuildBroadcastReceiverFlowImpl,
    ): BuildBroadcastReceiverFlow

    @Binds
    @Reusable
    abstract fun bindIsPermissionGranted(
        impl: IsPermissionGrantedImpl,
    ): IsPermissionGranted

    @Binds
    @Reusable
    abstract fun bindSaveUriToFile(
        impl: SaveUriToFileImpl,
    ): SaveUriToFile
}
