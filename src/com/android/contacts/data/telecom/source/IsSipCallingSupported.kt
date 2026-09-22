package com.android.contacts.data.telecom.source

import android.content.Context
import com.android.contacts.util.PhoneCapabilityTester
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface IsSipCallingSupported {
    operator fun invoke(): Boolean
}

internal class IsSipCallingSupportedImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : IsSipCallingSupported {

    override operator fun invoke(): Boolean {
        return PhoneCapabilityTester.isSipPhone(context)
    }
}
