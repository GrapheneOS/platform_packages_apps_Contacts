package com.android.contacts.domain.util

import android.content.Intent
import android.content.pm.PackageManager
import javax.inject.Inject

internal fun interface IsIntentRegistered {
    operator fun invoke(intent: Intent): Boolean
}

internal class IsIntentRegisteredImpl @Inject constructor(
    private val packageManager: PackageManager,
) : IsIntentRegistered {

    override operator fun invoke(intent: Intent): Boolean {
        val matches = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )

        return matches.isNotEmpty()
    }
}
