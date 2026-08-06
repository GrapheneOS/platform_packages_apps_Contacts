package com.android.contacts.di.debug

import com.android.contacts.domain.debug.usecase.ClearSeededTestData
import com.android.contacts.domain.debug.usecase.ClearSeededTestDataImpl
import com.android.contacts.domain.debug.usecase.ExportDatabase
import com.android.contacts.domain.debug.usecase.ExportDatabaseImpl
import com.android.contacts.domain.debug.usecase.GenerateTestContact
import com.android.contacts.domain.debug.usecase.GenerateTestContactImpl
import com.android.contacts.domain.debug.usecase.IsDebugEnabled
import com.android.contacts.domain.debug.usecase.IsDebugEnabledImpl
import com.android.contacts.domain.debug.usecase.IsExportDatabaseAvailable
import com.android.contacts.domain.debug.usecase.IsExportDatabaseAvailableImpl
import com.android.contacts.domain.debug.usecase.SeedTestData
import com.android.contacts.domain.debug.usecase.SeedTestDataImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DebugBindsModule {

    @Binds
    @Reusable
    abstract fun bindClearSeededTestData(
        impl: ClearSeededTestDataImpl,
    ): ClearSeededTestData

    @Binds
    @Reusable
    abstract fun bindExportDatabase(
        impl: ExportDatabaseImpl,
    ): ExportDatabase

    @Binds
    @Reusable
    abstract fun bindGenerateTestContact(
        impl: GenerateTestContactImpl,
    ): GenerateTestContact

    @Binds
    @Reusable
    abstract fun bindIsDebugEnabled(
        impl: IsDebugEnabledImpl,
    ): IsDebugEnabled

    @Binds
    @Reusable
    abstract fun bindIsExportDatabaseAvailable(
        impl: IsExportDatabaseAvailableImpl,
    ): IsExportDatabaseAvailable

    @Binds
    @Reusable
    abstract fun bindSeedTestData(
        impl: SeedTestDataImpl,
    ): SeedTestData
}
