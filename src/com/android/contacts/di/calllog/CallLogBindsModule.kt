package com.android.contacts.di.calllog

import com.android.contacts.data.calllog.repository.CallLogRepository
import com.android.contacts.data.calllog.repository.CallLogRepositoryImpl
import com.android.contacts.domain.calllog.usecase.GetRecentCalls
import com.android.contacts.domain.calllog.usecase.GetRecentCallsImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CallLogBindsModule {

    @Binds
    @Reusable
    abstract fun bindCallLogRepository(
        impl: CallLogRepositoryImpl,
    ): CallLogRepository

    @Binds
    @Reusable
    abstract fun bindGetRecentCalls(
        impl: GetRecentCallsImpl,
    ): GetRecentCalls
}
