package com.android.contacts.di.accounts

import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapperImpl
import com.android.contacts.domain.accounts.mapper.AccountFilterMapper
import com.android.contacts.domain.accounts.mapper.AccountFilterMapperImpl
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.mapper.AccountModelMapperImpl
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
    abstract fun bindAccountDisplayModelMapper(
        impl: AccountDisplayModelMapperImpl,
    ): AccountDisplayModelMapper

    @Binds
    @Reusable
    abstract fun bindAccountFilterMapper(
        impl: AccountFilterMapperImpl,
    ): AccountFilterMapper

    @Binds
    @Reusable
    abstract fun bindAccountModelMapper(
        impl: AccountModelMapperImpl,
    ): AccountModelMapper

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
