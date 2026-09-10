package com.android.contacts.data.contactdetails.source

import android.content.Context
import android.media.RingtoneManager
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface RingtoneTitleSource {
    operator fun invoke(ringtone: String?): String?
}

internal class RingtoneTitleSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RingtoneTitleSource {

    override operator fun invoke(ringtone: String?): String? {
        if (ringtone.isNullOrEmpty()) {
            return null
        }

        return RingtoneManager.getRingtone(context, ringtone.toUri())?.getTitle(context)
    }
}
