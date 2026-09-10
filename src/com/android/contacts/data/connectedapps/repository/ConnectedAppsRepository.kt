package com.android.contacts.data.connectedapps.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.ContactsContract.Data
import com.android.contacts.data.connectedapps.model.ConnectedApp
import com.android.contacts.util.core.resourceUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface ConnectedAppsRepository {
    fun getConnectedApp(
        dataId: Long,
        mimeType: String,
    ): ConnectedApp?
}

internal class ConnectedAppsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
) : ConnectedAppsRepository {

    override fun getConnectedApp(
        dataId: Long,
        mimeType: String,
    ): ConnectedApp? {
        val applicationInfo = resolveApplication(dataId, mimeType)
            ?.takeIf { info -> info.packageName != context.packageName }
        val label = applicationInfo
            ?.loadLabel(packageManager)
            ?.toString()
            ?.trim()
            ?.takeIf { value -> value.isNotEmpty() }

        return when {
            applicationInfo != null && label != null -> ConnectedApp(
                packageName = applicationInfo.packageName,
                label = label,
                iconUri = resourceUri(
                    packageName = applicationInfo.packageName,
                    resourceId = applicationInfo.icon,
                ),
            )

            else -> null
        }
    }

    private fun resolveApplication(
        dataId: Long,
        mimeType: String,
    ): ApplicationInfo? {
        val dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, dataId)
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(dataUri, mimeType)
        val flags = PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())

        return packageManager.resolveActivity(intent, flags)
            ?.activityInfo
            ?.applicationInfo
    }
}
