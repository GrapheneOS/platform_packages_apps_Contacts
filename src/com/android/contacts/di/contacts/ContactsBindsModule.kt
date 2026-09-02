package com.android.contacts.di.contacts

import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.data.contacts.repository.ContactsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ContactsBindsModule {

    @Binds
    @Reusable
    abstract fun bindContactsRepository(
        impl: ContactsRepositoryImpl,
    ): ContactsRepository
}
