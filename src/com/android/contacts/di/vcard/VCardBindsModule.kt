package com.android.contacts.di.vcard

import com.android.contacts.domain.vcard.usecase.BuildVCardSource
import com.android.contacts.domain.vcard.usecase.BuildVCardSourceImpl
import com.android.contacts.domain.vcard.usecase.CanImportFromVCard
import com.android.contacts.domain.vcard.usecase.CanImportFromVCardImpl
import com.android.contacts.domain.vcard.usecase.ImportVCards
import com.android.contacts.domain.vcard.usecase.ImportVCardsImpl
import com.android.contacts.domain.vcard.usecase.ParseVCardDetails
import com.android.contacts.domain.vcard.usecase.ParseVCardDetailsImpl
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayName
import com.android.contacts.domain.vcard.usecase.ResolveFileDisplayNameImpl
import com.android.contacts.domain.vcard.usecase.VCardServiceRunner
import com.android.contacts.domain.vcard.usecase.VCardServiceRunnerImpl
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
    abstract fun bindBuildVCardSource(
        impl: BuildVCardSourceImpl,
    ): BuildVCardSource

    @Binds
    @Reusable
    abstract fun bindCanImportFromVCard(
        impl: CanImportFromVCardImpl,
    ): CanImportFromVCard

    @Binds
    @Reusable
    abstract fun bindImportVCards(
        impl: ImportVCardsImpl,
    ): ImportVCards

    @Binds
    @Reusable
    abstract fun bindParseVCardDetails(
        impl: ParseVCardDetailsImpl,
    ): ParseVCardDetails

    @Binds
    @Reusable
    abstract fun bindResolveFileDisplayName(
        impl: ResolveFileDisplayNameImpl,
    ): ResolveFileDisplayName

    @Binds
    @Reusable
    abstract fun bindVCardServiceRunner(
        impl: VCardServiceRunnerImpl,
    ): VCardServiceRunner
}
