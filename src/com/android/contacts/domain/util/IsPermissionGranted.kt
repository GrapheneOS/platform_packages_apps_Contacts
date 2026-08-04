package com.android.contacts.domain.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal fun interface IsPermissionGranted {
    operator fun invoke(permission: String): Boolean
}

internal class IsPermissionGrantedImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : IsPermissionGranted {
    override fun invoke(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
