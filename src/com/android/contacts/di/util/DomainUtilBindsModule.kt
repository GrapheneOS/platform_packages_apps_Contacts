package com.android.contacts.di.util

import com.android.contacts.domain.util.CanVideoCall
import com.android.contacts.domain.util.CanVideoCallImpl
import com.android.contacts.domain.util.GetIntentLabel
import com.android.contacts.domain.util.GetIntentLabelImpl
import com.android.contacts.domain.util.IsCallWithNoteSupported
import com.android.contacts.domain.util.IsCallWithNoteSupportedImpl
import com.android.contacts.domain.util.IsDeviceVoiceCapable
import com.android.contacts.domain.util.IsDeviceVoiceCapableImpl
import com.android.contacts.domain.util.IsIntentRegistered
import com.android.contacts.domain.util.IsIntentRegisteredImpl
import com.android.contacts.domain.util.IsSipCallingSupported
import com.android.contacts.domain.util.IsSipCallingSupportedImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainUtilBindsModule {

    @Binds
    @Reusable
    abstract fun bindCanVideoCall(
        impl: CanVideoCallImpl,
    ): CanVideoCall

    @Binds
    @Reusable
    abstract fun bindGetIntentLabel(
        impl: GetIntentLabelImpl,
    ): GetIntentLabel

    @Binds
    @Reusable
    abstract fun bindIsCallWithNoteSupported(
        impl: IsCallWithNoteSupportedImpl,
    ): IsCallWithNoteSupported

    @Binds
    @Reusable
    abstract fun bindIsDeviceVoiceCapable(
        impl: IsDeviceVoiceCapableImpl,
    ): IsDeviceVoiceCapable

    @Binds
    @Reusable
    abstract fun bindIsIntentRegistered(
        impl: IsIntentRegisteredImpl,
    ): IsIntentRegistered

    @Binds
    @Reusable
    abstract fun bindIsSipCallingSupported(
        impl: IsSipCallingSupportedImpl,
    ): IsSipCallingSupported
}
