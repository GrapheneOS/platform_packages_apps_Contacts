package com.android.contacts.domain.vcard.usecase

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import com.android.contacts.R
import com.android.contacts.domain.vcard.model.ExportConfig
import com.android.contacts.vcard.VCardService
import javax.inject.Inject

internal fun interface GetExportConfig {
    operator fun invoke(): ExportConfig
}

internal class GetExportConfigImpl @Inject constructor(
    private val resources: Resources,
    private val packageManager: PackageManager,
) : GetExportConfig {
    override fun invoke(): ExportConfig {
        return ExportConfig(
            canExportContacts = resources.getBoolean(R.bool.config_allow_export) &&
                canCreateDocument(),
            canShareContacts = resources.getBoolean(R.bool.config_allow_share_contacts),
        )
    }

    private fun canCreateDocument(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(VCardService.X_VCARD_MIME_TYPE)

        val receivers = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return receivers.isNotEmpty()
    }
}
