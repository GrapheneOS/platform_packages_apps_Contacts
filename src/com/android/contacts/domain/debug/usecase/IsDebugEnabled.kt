package com.android.contacts.domain.debug.usecase

import com.android.contacts.BuildConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

internal fun interface IsDebugEnabled {
    operator fun invoke(): Boolean

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Provider {
        fun isDebugEnabled(): IsDebugEnabled
    }
}

internal class IsDebugEnabledImpl @Inject constructor() : IsDebugEnabled {
    override fun invoke(): Boolean {
        return BuildConfig.DEBUG
    }
}
