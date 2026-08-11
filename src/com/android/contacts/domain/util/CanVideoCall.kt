package com.android.contacts.domain.util

import android.content.Context
import com.android.contacts.CallUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface CanVideoCall {
    operator fun invoke(isCarrierVideoCallCapable: Boolean): Boolean
}

internal class CanVideoCallImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CanVideoCall {

    override operator fun invoke(isCarrierVideoCallCapable: Boolean): Boolean {
        val availability = CallUtil.getVideoCallingAvailability(context)
        val isEnabled = availability and CallUtil.VIDEO_CALLING_ENABLED != 0
        val requiresPresence = availability and CallUtil.VIDEO_CALLING_PRESENCE != 0

        return isEnabled && (!requiresPresence || isCarrierVideoCallCapable)
    }
}
