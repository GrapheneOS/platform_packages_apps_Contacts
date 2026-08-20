package com.android.contacts.di.core

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.text.BidiFormatter
import com.android.contacts.util.concurrent.ContactsExecutors
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal class CoreProvidesModule {

    // Coroutine Dispatchers

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

    // Others

    @Provides
    @Reusable
    fun provideBidiFormatter(): BidiFormatter {
        return BidiFormatter.getInstance()
    }

    @Provides
    @Reusable
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Reusable
    fun providePackageManager(
        @ApplicationContext context: Context,
    ): PackageManager {
        return context.packageManager
    }

    @Provides
    @Reusable
    fun provideRandom(): Random {
        return Random
    }

    @Provides
    @Reusable
    fun provideSubscriptionManager(
        @ApplicationContext context: Context,
    ): SubscriptionManager {
        return context.getSystemService(SubscriptionManager::class.java)
    }
}
