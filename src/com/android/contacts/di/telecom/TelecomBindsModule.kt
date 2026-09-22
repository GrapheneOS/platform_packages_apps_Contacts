package com.android.contacts.di.telecom

import com.android.contacts.data.telecom.repository.PhoneAccountsRepository
import com.android.contacts.data.telecom.repository.PhoneAccountsRepositoryImpl
import com.android.contacts.data.telecom.source.IsCallWithNoteSupported
import com.android.contacts.data.telecom.source.IsCallWithNoteSupportedImpl
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapableImpl
import com.android.contacts.data.telecom.source.IsSipCallingSupported
import com.android.contacts.data.telecom.source.IsSipCallingSupportedImpl
import com.android.contacts.data.telecom.source.VideoCallingCapabilitySource
import com.android.contacts.data.telecom.source.VideoCallingCapabilitySourceImpl
import com.android.contacts.domain.telecom.usecase.CanVideoCall
import com.android.contacts.domain.telecom.usecase.CanVideoCallImpl
import com.android.contacts.domain.telecom.usecase.GetCallingSimOptions
import com.android.contacts.domain.telecom.usecase.GetCallingSimOptionsImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TelecomBindsModule {

    @Binds
    @Reusable
    abstract fun bindPhoneAccountsRepository(
        impl: PhoneAccountsRepositoryImpl,
    ): PhoneAccountsRepository

    @Binds
    @Reusable
    abstract fun bindIsDeviceVoiceCapable(
        impl: IsDeviceVoiceCapableImpl,
    ): IsDeviceVoiceCapable

    @Binds
    @Reusable
    abstract fun bindIsSipCallingSupported(
        impl: IsSipCallingSupportedImpl,
    ): IsSipCallingSupported

    @Binds
    @Reusable
    abstract fun bindIsCallWithNoteSupported(
        impl: IsCallWithNoteSupportedImpl,
    ): IsCallWithNoteSupported

    @Binds
    @Reusable
    abstract fun bindVideoCallingCapabilitySource(
        impl: VideoCallingCapabilitySourceImpl,
    ): VideoCallingCapabilitySource

    @Binds
    @Reusable
    abstract fun bindCanVideoCall(
        impl: CanVideoCallImpl,
    ): CanVideoCall

    @Binds
    @Reusable
    abstract fun bindGetCallingSimOptions(
        impl: GetCallingSimOptionsImpl,
    ): GetCallingSimOptions
}
