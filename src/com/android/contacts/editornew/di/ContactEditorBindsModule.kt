package com.android.contacts.editornew.di

import com.android.contacts.editornew.contact.ContactDelegate
import com.android.contacts.editornew.contact.ContactDelegateImpl
import com.android.contacts.editornew.photo.picker.PhotoDelegate
import com.android.contacts.editornew.photo.picker.PhotoDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ContactEditorBindsModule {

    @Binds
    abstract fun bindPhotoDelegate(
        impl: PhotoDelegateImpl,
    ): PhotoDelegate

    @Binds
    abstract fun bindContactDelegate(
        impl: ContactDelegateImpl,
    ): ContactDelegate
}
