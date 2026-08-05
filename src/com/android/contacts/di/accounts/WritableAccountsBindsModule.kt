package com.android.contacts.di.accounts

import com.android.contacts.domain.accounts.usecase.LoadWritableAccounts
import com.android.contacts.domain.accounts.usecase.LoadWritableAccountsImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WritableAccountsBindsModule {

    @Binds
    @Reusable
    abstract fun bindLoadWritableAccounts(
        impl: LoadWritableAccountsImpl,
    ): LoadWritableAccounts
}
