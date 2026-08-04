package com.android.contacts.di.util

import com.android.contacts.domain.util.AcquireWakeLock
import com.android.contacts.domain.util.AcquireWakeLockImpl
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.domain.util.BuildBroadcastReceiverFlowImpl
import com.android.contacts.domain.util.IsPermissionGranted
import com.android.contacts.domain.util.IsPermissionGrantedImpl
import com.android.contacts.domain.util.SaveUriToFile
import com.android.contacts.domain.util.SaveUriToFileImpl
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
