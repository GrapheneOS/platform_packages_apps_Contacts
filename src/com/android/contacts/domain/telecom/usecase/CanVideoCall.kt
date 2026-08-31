package com.android.contacts.domain.telecom.usecase

import com.android.contacts.data.telecom.source.VideoCallingCapabilitySource
import javax.inject.Inject

internal fun interface CanVideoCall {
    operator fun invoke(isCarrierVideoCallCapable: Boolean): Boolean
}

internal class CanVideoCallImpl @Inject constructor(
    private val videoCallingCapabilitySource: VideoCallingCapabilitySource,
) : CanVideoCall {

    override operator fun invoke(isCarrierVideoCallCapable: Boolean): Boolean {
        val capability = videoCallingCapabilitySource()

        return capability.isEnabled && (!capability.requiresPresence || isCarrierVideoCallCapable)
    }
}
