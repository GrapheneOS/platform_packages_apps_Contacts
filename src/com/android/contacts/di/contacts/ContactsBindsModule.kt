package com.android.contacts.di.contacts

import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.data.contacts.repository.ContactsRepositoryImpl
import com.android.contacts.domain.accounts.mapper.ContactsAccountFilterMapper
import com.android.contacts.domain.accounts.mapper.ContactsAccountFilterMapperImpl
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapper
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapperImpl
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.domain.contacts.usecase.LoadRawContactsImpl
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapper
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapperImpl
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
    abstract fun bindContactsAccountFilterMapper(
        impl: ContactsAccountFilterMapperImpl,
    ): ContactsAccountFilterMapper

    @Binds
    @Reusable
    abstract fun bindContactsRepository(
        impl: ContactsRepositoryImpl,
    ): ContactsRepository

    @Binds
    @Reusable
    abstract fun bindLoadRawContacts(
        impl: LoadRawContactsImpl,
    ): LoadRawContacts

    @Binds
    @Reusable
    abstract fun bindRawContactWithAccountMapper(
        impl: RawContactWithAccountMapperImpl,
    ): RawContactWithAccountMapper

    @Binds
    @Reusable
    abstract fun bindRawContactUiModelMapper(
        impl: RawContactUiModelMapperImpl,
    ): RawContactUiModelMapper
}
