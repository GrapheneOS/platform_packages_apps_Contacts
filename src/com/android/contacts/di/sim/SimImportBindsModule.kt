package com.android.contacts.di.sim

import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapper
import com.android.contacts.ui.simimport.screen.mapper.SimContactUiModelMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SimImportBindsModule {

    @Binds
    @Reusable
    abstract fun bindSimContactUiModelMapper(
        impl: SimContactUiModelMapperImpl,
    ): SimContactUiModelMapper
}
