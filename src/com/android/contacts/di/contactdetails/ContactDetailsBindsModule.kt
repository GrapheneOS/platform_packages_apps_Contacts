package com.android.contacts.di.contactdetails

import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepositoryImpl
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactoryImpl
import com.android.contacts.data.contactdetails.mapper.ContactDetailsMapper
import com.android.contacts.data.contactdetails.mapper.ContactDetailsMapperImpl
import com.android.contacts.data.contactdetails.mapper.DataItemCollapseMatcher
import com.android.contacts.data.contactdetails.mapper.DataItemCollapseMatcherImpl
import com.android.contacts.data.contactdetails.mapper.DataItemCollapser
import com.android.contacts.data.contactdetails.mapper.DataItemCollapserImpl
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import com.android.contacts.data.contactdetails.repository.ContactActionsRepositoryImpl
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepositoryImpl
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepositoryImpl
import com.android.contacts.data.contactdetails.source.ContactLoaderSource
import com.android.contacts.data.contactdetails.source.ContactLoaderSourceImpl
import com.android.contacts.domain.contactdetails.usecase.BuildContactDetailsCards
import com.android.contacts.domain.contactdetails.usecase.BuildContactDetailsCardsImpl
import com.android.contacts.domain.contactdetails.usecase.GetContactDetailsMenu
import com.android.contacts.domain.contactdetails.usecase.GetContactDetailsMenuImpl
import com.android.contacts.data.calllog.repository.CallLogRepository
import com.android.contacts.data.telecom.repository.PhoneAccountsRepository
import com.android.contacts.data.telecom.repository.PhoneAccountsRepositoryImpl
import com.android.contacts.data.calllog.repository.CallLogRepositoryImpl
import com.android.contacts.domain.calllog.usecase.GetRecentCalls
import com.android.contacts.domain.telecom.usecase.GetCallingSimOptions
import com.android.contacts.domain.telecom.usecase.GetCallingSimOptionsImpl
import com.android.contacts.domain.calllog.usecase.GetRecentCallsImpl
import com.android.contacts.domain.contactdetails.usecase.GetContactQuickActions
import com.android.contacts.domain.contactdetails.usecase.GetContactQuickActionsImpl
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailable
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailableImpl
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapper
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapperImpl
import com.android.contacts.ui.contactdetails.screen.mapper.ContactQuickActionsMapper
import com.android.contacts.ui.contactdetails.screen.mapper.RecentCallsMapper
import com.android.contacts.ui.contactdetails.screen.mapper.RecentCallsMapperImpl
import com.android.contacts.ui.contactdetails.screen.mapper.ContactQuickActionsMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ContactDetailsBindsModule {

    @Binds
    @Reusable
    abstract fun bindDataItemCollapseMatcher(
        impl: DataItemCollapseMatcherImpl,
    ): DataItemCollapseMatcher

    @Binds
    @Reusable
    abstract fun bindDataItemCollapser(
        impl: DataItemCollapserImpl,
    ): DataItemCollapser

    @Binds
    @Reusable
    abstract fun bindContactDetailsMapper(
        impl: ContactDetailsMapperImpl,
    ): ContactDetailsMapper

    @Binds
    @Reusable
    abstract fun bindContactEntryIntentFactory(
        impl: ContactEntryIntentFactoryImpl,
    ): ContactEntryIntentFactory

    @Binds
    @Reusable
    abstract fun bindContactLoaderSource(
        impl: ContactLoaderSourceImpl,
    ): ContactLoaderSource

    @Binds
    @Singleton
    abstract fun bindContactDetailsRepository(
        impl: ContactDetailsRepositoryImpl,
    ): ContactDetailsRepository

    @Binds
    @Reusable
    abstract fun bindContactActionsRepository(
        impl: ContactActionsRepositoryImpl,
    ): ContactActionsRepository

    @Binds
    @Reusable
    abstract fun bindContactShortcutRepository(
        impl: ContactShortcutRepositoryImpl,
    ): ContactShortcutRepository

    @Binds
    @Reusable
    abstract fun bindBuildContactDetailsCards(
        impl: BuildContactDetailsCardsImpl,
    ): BuildContactDetailsCards

    @Binds
    @Reusable
    abstract fun bindContactDetailsUiStateMapper(
        impl: ContactDetailsUiStateMapperImpl,
    ): ContactDetailsUiStateMapper

    @Binds
    @Reusable
    abstract fun bindGetContactQuickActions(
        impl: GetContactQuickActionsImpl,
    ): GetContactQuickActions

    @Binds
    @Reusable
    abstract fun bindPhoneAccountsRepository(
        impl: PhoneAccountsRepositoryImpl,
    ): PhoneAccountsRepository

    @Binds
    @Reusable
    abstract fun bindGetCallingSimOptions(
        impl: GetCallingSimOptionsImpl,
    ): GetCallingSimOptions

    @Binds
    @Reusable
    abstract fun bindCallLogRepository(
        impl: CallLogRepositoryImpl,
    ): CallLogRepository

    @Binds
    @Reusable
    abstract fun bindGetRecentCalls(
        impl: GetRecentCallsImpl,
    ): GetRecentCalls

    @Binds
    @Reusable
    abstract fun bindRecentCallsMapper(
        impl: RecentCallsMapperImpl,
    ): RecentCallsMapper

    @Binds
    abstract fun bindContactQuickActionsMapper(
        impl: ContactQuickActionsMapperImpl,
    ): ContactQuickActionsMapper

    @Binds
    @Reusable
    abstract fun bindGetContactDetailsMenu(
        impl: GetContactDetailsMenuImpl,
    ): GetContactDetailsMenu

    @Binds
    @Reusable
    abstract fun bindConnectedAppsRepository(
        impl: ConnectedAppsRepositoryImpl,
    ): ConnectedAppsRepository

    @Binds
    @Reusable
    abstract fun bindIsEntryActionAvailable(
        impl: IsEntryActionAvailableImpl,
    ): IsEntryActionAvailable
}
