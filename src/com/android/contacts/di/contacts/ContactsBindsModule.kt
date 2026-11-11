package com.android.contacts.di.contacts

import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.data.contacts.repository.ContactsRepositoryImpl
import com.android.contacts.data.contacts.repository.DirectoriesRepository
import com.android.contacts.data.contacts.repository.DirectoriesRepositoryImpl
import com.android.contacts.domain.accounts.mapper.ContactsAccountFilterMapper
import com.android.contacts.domain.accounts.mapper.ContactsAccountFilterMapperImpl
import com.android.contacts.domain.contacts.mapper.ContactDirectoryMapper
import com.android.contacts.domain.contacts.mapper.ContactDirectoryMapperImpl
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.mapper.ContactItemMapperImpl
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapper
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapperImpl
import com.android.contacts.domain.contacts.usecase.GetContact
import com.android.contacts.domain.contacts.usecase.GetContactImpl
import com.android.contacts.domain.contacts.usecase.GetContactsForSelection
import com.android.contacts.domain.contacts.usecase.GetContactsForSelectionImpl
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.domain.contacts.usecase.LoadRawContactsImpl
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapper
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapperImpl
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionEffectMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionEffectMapperImpl
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionFilterMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionFilterMapperImpl
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionModeMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionModeMapperImpl
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionUiStateMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionUiStateMapperImpl
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
    abstract fun bindContactSelectionEffectMapper(
        impl: ContactSelectionEffectMapperImpl,
    ): ContactSelectionEffectMapper

    @Binds
    @Reusable
    abstract fun bindContactSelectionFilterMapper(
        impl: ContactSelectionFilterMapperImpl,
    ): ContactSelectionFilterMapper

    @Binds
    @Reusable
    abstract fun bindContactSelectionModeMapper(
        impl: ContactSelectionModeMapperImpl,
    ): ContactSelectionModeMapper

    @Binds
    @Reusable
    abstract fun bindContactSelectionUiStateMapper(
        impl: ContactSelectionUiStateMapperImpl,
    ): ContactSelectionUiStateMapper

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
    abstract fun bindDirectoriesRepository(
        impl: DirectoriesRepositoryImpl,
    ): DirectoriesRepository

    @Binds
    @Reusable
    abstract fun bindGetContact(
        impl: GetContactImpl,
    ): GetContact

    @Binds
    @Reusable
    abstract fun bindGetContactsForSelection(
        impl: GetContactsForSelectionImpl,
    ): GetContactsForSelection

    @Binds
    @Reusable
    abstract fun bindLoadRawContacts(
        impl: LoadRawContactsImpl,
    ): LoadRawContacts

    @Binds
    @Reusable
    abstract fun bindRawContactUiModelMapper(
        impl: RawContactUiModelMapperImpl,
    ): RawContactUiModelMapper

    @Binds
    @Reusable
    abstract fun bindRawContactWithAccountMapper(
        impl: RawContactWithAccountMapperImpl,
    ): RawContactWithAccountMapper

    @Binds
    @Reusable
    abstract fun bindContactItemMapper(
        impl: ContactItemMapperImpl,
    ): ContactItemMapper

    @Binds
    @Reusable
    abstract fun bindContactDirectoryMapper(
        impl: ContactDirectoryMapperImpl,
    ): ContactDirectoryMapper
}
