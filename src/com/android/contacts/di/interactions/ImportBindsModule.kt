package com.android.contacts.di.interactions

import com.android.contacts.ui.interactions.importing.screen.ImportExternalEventManager
import com.android.contacts.ui.interactions.importing.screen.ImportExternalEventManagerImpl
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ImportBindsModule {

    @Binds
    @Singleton
    abstract fun bindImportExternalEventManager(
        impl: ImportExternalEventManagerImpl,
    ): ImportExternalEventManager

    @Binds
    @Reusable
    abstract fun bindSimCardOptionMapper(
        impl: SimCardOptionMapperImpl,
    ): SimCardOptionMapper
}
