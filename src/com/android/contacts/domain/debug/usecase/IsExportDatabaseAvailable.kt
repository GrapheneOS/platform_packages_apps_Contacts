package com.android.contacts.domain.debug.usecase

import android.content.Intent
import android.content.pm.PackageManager
import javax.inject.Inject

internal fun interface IsExportDatabaseAvailable {
    operator fun invoke(): Boolean
}

internal class IsExportDatabaseAvailableImpl @Inject constructor(
    private val packageManager: PackageManager,
) : IsExportDatabaseAvailable {
    override fun invoke(): Boolean {
        val receivers = packageManager
            .queryIntentActivities(
                Intent("com.android.providers.contacts.DUMP_DATABASE"),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        return receivers.isNotEmpty()
    }
}
