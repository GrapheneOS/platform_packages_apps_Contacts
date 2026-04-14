package com.android.contacts.ui.contactcreation.di

import android.content.Context
import com.android.contacts.model.AccountTypeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ContactCreationProvidesModule {

    @Provides
    @Singleton
    fun provideAccountTypeManager(
        @ApplicationContext context: Context,
    ): AccountTypeManager = AccountTypeManager.getInstance(context)
}
