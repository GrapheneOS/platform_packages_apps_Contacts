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

    // AccountTypeManager.getInstance() returns an app-global singleton.
    // @Singleton matches the actual lifecycle of the underlying Java object.
    // ViewModelScoped or ActivityScoped would create new instances that
    // just delegate to the same singleton, adding indirection for no benefit.
    @Provides
    @Singleton
    fun provideAccountTypeManager(
        @ApplicationContext context: Context,
    ): AccountTypeManager = AccountTypeManager.getInstance(context)
}
