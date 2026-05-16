package com.android.contacts.editornew.di

import android.content.Context
import com.android.contacts.editor.ContactEditorUtils
import com.android.contacts.editornew.photo.picker.PhotoDelegateHelper
import com.android.contacts.model.AccountTypeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
internal object ContactEditorModule {

    @Provides
    fun provideAccountTypeManager(
        @ApplicationContext
        context: Context,
    ): AccountTypeManager = AccountTypeManager.getInstance(context)

    @Provides
    fun provideContactEditorUtils(
        @ApplicationContext
        context: Context,
    ): ContactEditorUtils = ContactEditorUtils.create(context)

    @Provides
    fun providePhotoDelegateHelper(
        @ApplicationContext
        context: Context,
    ): PhotoDelegateHelper = PhotoDelegateHelper(context)
}
