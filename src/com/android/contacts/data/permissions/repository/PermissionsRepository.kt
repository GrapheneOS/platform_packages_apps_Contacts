package com.android.contacts.data.permissions.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface PermissionsRepository {
    suspend fun isCallLogGranted(): Boolean
}

internal class PermissionsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PermissionsRepository {

    override suspend fun isCallLogGranted(): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
    }
}
