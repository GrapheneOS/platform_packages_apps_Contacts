package com.android.contacts.di.core

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.contacts.database.SimContactDao
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.preference.ContactsPreferences
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class DataProvidesModule {

    @Provides
    @Reusable
    fun contactsPreferences(
        @ApplicationContext context: Context,
    ): ContactsPreferences {
        return ContactsPreferences(context)
    }

    @Provides
    @Reusable
    fun accountTypeManager(
        @ApplicationContext context: Context,
    ): AccountTypeManager {
        return AccountTypeManager.getInstance(context)
    }

    @Provides
    @Reusable
    fun simContactDao(
        @ApplicationContext context: Context,
    ): SimContactDao {
        return SimContactDao.create(context)
    }

    @Provides
    @Reusable
    fun localBroadcastManager(
        @ApplicationContext context: Context,
    ): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(context)
    }
}
