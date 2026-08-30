package com.android.contacts.di.contactdetails

import com.android.contacts.ui.contactdetails.screen.delegate.ContactDetailsContentDelegate
import com.android.contacts.ui.contactdetails.screen.delegate.ContactDetailsContentDelegateImpl
import com.android.contacts.ui.contactdetails.screen.delegate.ContactFlagsDelegate
import com.android.contacts.ui.contactdetails.screen.delegate.ContactFlagsDelegateImpl
import com.android.contacts.ui.contactdetails.screen.delegate.ContactLinkDelegate
import com.android.contacts.ui.contactdetails.screen.delegate.ContactLinkDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ContactDetailsViewModelBindsModule {

    @Binds
    @ViewModelScoped
    abstract fun bindContactDetailsContentDelegate(
        impl: ContactDetailsContentDelegateImpl,
    ): ContactDetailsContentDelegate

    @Binds
    @ViewModelScoped
    abstract fun bindContactFlagsDelegate(
        impl: ContactFlagsDelegateImpl,
    ): ContactFlagsDelegate

    @Binds
    @ViewModelScoped
    abstract fun bindContactLinkDelegate(
        impl: ContactLinkDelegateImpl,
    ): ContactLinkDelegate
}
