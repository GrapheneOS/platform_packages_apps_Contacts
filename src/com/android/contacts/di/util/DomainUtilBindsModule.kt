package com.android.contacts.di.util

import com.android.contacts.domain.util.GetIntentLabel
import com.android.contacts.domain.util.GetIntentLabelImpl
import com.android.contacts.domain.util.IsDeviceVoiceCapable
import com.android.contacts.domain.util.IsDeviceVoiceCapableImpl
import com.android.contacts.domain.util.IsIntentRegistered
import com.android.contacts.domain.util.IsIntentRegisteredImpl
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
    abstract fun bindGetIntentLabel(
        impl: GetIntentLabelImpl,
    ): GetIntentLabel

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
}
