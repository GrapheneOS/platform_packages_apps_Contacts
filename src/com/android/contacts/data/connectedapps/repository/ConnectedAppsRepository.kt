package com.android.contacts.data.connectedapps.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.Data
import com.android.contacts.data.connectedapps.model.ConnectedApp
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
        val applicationInfo = resolveApplication(dataId, mimeType) ?: return null
        if (applicationInfo.packageName == context.packageName) {
            return null
        }

        val label = applicationInfo.loadLabel(packageManager).toString().trim()
        if (label.isEmpty()) {
            return null
        }

        return ConnectedApp(
            packageName = applicationInfo.packageName,
            label = label,
            iconUri = iconUri(applicationInfo),
        )
    }

    private fun resolveApplication(
        dataId: Long,
        mimeType: String,
    ): ApplicationInfo? {
        val dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, dataId)
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(dataUri, mimeType)
        val flags = PackageManager.ResolveInfoFlags.of(MATCH_DEFAULT.toLong())

        return packageManager.resolveActivity(intent, flags)
            ?.activityInfo
            ?.applicationInfo
    }

    private fun iconUri(applicationInfo: ApplicationInfo): String? {
        val iconResourceId = applicationInfo.icon
        if (iconResourceId == NO_ICON) {
            return null
        }

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(applicationInfo.packageName)
            .appendPath(iconResourceId.toString())
            .build()
            .toString()
    }

    private companion object {
        const val MATCH_DEFAULT = PackageManager.MATCH_DEFAULT_ONLY
        const val NO_ICON = 0
    }
}
