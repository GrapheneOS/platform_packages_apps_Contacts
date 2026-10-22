package com.android.contacts.di.callsubject

import com.android.contacts.data.callsubject.repository.CallSubjectRepository
import com.android.contacts.data.callsubject.repository.CallSubjectRepositoryImpl
import com.android.contacts.data.callsubject.repository.GetCallSubjectConfiguration
import com.android.contacts.data.callsubject.repository.GetCallSubjectConfigurationImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CallSubjectBindsModule {

    @Binds
    @Reusable
    abstract fun bindCallSubjectRepository(
        impl: CallSubjectRepositoryImpl,
    ): CallSubjectRepository

    @Binds
    @Reusable
    abstract fun GetCallSubjectConfiguration(
        impl: GetCallSubjectConfigurationImpl,
    ): GetCallSubjectConfiguration
}
