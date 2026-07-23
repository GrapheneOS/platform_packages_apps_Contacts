package com.android.contacts.di.core

import androidx.core.text.BidiFormatter
import com.android.contacts.util.concurrent.ContactsExecutors
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal class CoreProvidesModule {

    @Provides
    @Reusable
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }

    @Provides
    @Reusable
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @Provides
    @Reusable
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }

    @Provides
    @Reusable
    @SimReadDispatcher
    fun provideSimReadDispatcher(): CoroutineDispatcher {
        return ContactsExecutors.getSimReadExecutor().asCoroutineDispatcher()
    }

    @Provides
    @Reusable
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }
}
