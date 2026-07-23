package com.android.contacts.di.interactions

import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapper
import com.android.contacts.ui.interactions.importing.screen.mapper.SimCardOptionMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ImportBindsModule {

    @Binds
    @Reusable
    abstract fun bindSimCardOptionMapper(
        impl: SimCardOptionMapperImpl,
    ): SimCardOptionMapper
}
