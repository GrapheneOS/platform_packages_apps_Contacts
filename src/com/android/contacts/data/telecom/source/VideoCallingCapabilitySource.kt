package com.android.contacts.data.telecom.source

import android.content.Context
import com.android.contacts.CallUtil
import com.android.contacts.data.telecom.model.VideoCallingCapability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface VideoCallingCapabilitySource {
    operator fun invoke(): VideoCallingCapability
}

internal class VideoCallingCapabilitySourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VideoCallingCapabilitySource {

    override operator fun invoke(): VideoCallingCapability {
        val availability = CallUtil.getVideoCallingAvailability(context)

        return VideoCallingCapability(
            isEnabled = availability and CallUtil.VIDEO_CALLING_ENABLED != 0,
            requiresPresence = availability and CallUtil.VIDEO_CALLING_PRESENCE != 0,
        )
    }
}
