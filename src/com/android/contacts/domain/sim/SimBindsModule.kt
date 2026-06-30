package com.android.contacts.domain.sim

import com.android.contacts.domain.sim.usecase.LoadSimContacts
import com.android.contacts.domain.sim.usecase.LoadSimContactsImpl
import com.android.contacts.domain.sim.usecase.StartSimImport
import com.android.contacts.domain.sim.usecase.StartSimImportImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SimBindsModule {

    @Binds
    @Reusable
    abstract fun bindLoadSimContacts(
        impl: LoadSimContactsImpl,
    ): LoadSimContacts

    @Binds
    @Reusable
    abstract fun bindStartSimImport(
        impl: StartSimImportImpl,
    ): StartSimImport
}
