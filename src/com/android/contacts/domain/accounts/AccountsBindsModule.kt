package com.android.contacts.domain.accounts

import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.GetDefaultAccountImpl
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.accounts.usecase.LoadAccountsImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AccountsBindsModule {

    @Binds
    @Reusable
    abstract fun bindGetDefaultAccount(
        impl: GetDefaultAccountImpl,
    ): GetDefaultAccount

    @Binds
    @Reusable
    abstract fun bindLoadAccounts(
        impl: LoadAccountsImpl,
    ): LoadAccounts
}
