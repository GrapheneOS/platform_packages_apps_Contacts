package com.android.contacts.domain.contactdetails.usecase.telephony

import android.telephony.TelephonyManager
import javax.inject.Inject

internal fun interface IsDeviceVoiceCapable {
    operator fun invoke(): Boolean
}

internal class IsDeviceVoiceCapableImpl @Inject constructor(
    private val telephonyManager: TelephonyManager,
) : IsDeviceVoiceCapable {

    override operator fun invoke(): Boolean {
        return telephonyManager.isVoiceCapable
    }
}
