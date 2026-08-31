package com.android.contacts.di.connectedapps

import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConnectedAppsBindsModule {

    @Binds
    @Reusable
    abstract fun bindConnectedAppsRepository(
        impl: ConnectedAppsRepositoryImpl,
    ): ConnectedAppsRepository
}
