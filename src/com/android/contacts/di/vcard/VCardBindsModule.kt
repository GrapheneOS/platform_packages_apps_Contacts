package com.android.contacts.di.vcard

import com.android.contacts.domain.vcard.usecase.CanImportFromVCard
import com.android.contacts.domain.vcard.usecase.CanImportFromVCardImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class VCardBindsModule {

    @Binds
    @Reusable
    abstract fun bindCanImportFromVCard(
        impl: CanImportFromVCardImpl,
    ): CanImportFromVCard
}
