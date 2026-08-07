package com.android.contacts.data.appinfo.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface AppInfoRepository {
    suspend fun getBuildVersion(): String?
}

internal class AppInfoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppInfoRepository {

    override suspend fun getBuildVersion(): String? {
        return withContext(ioDispatcher) {
            try {
                packageManager
                    .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                    .versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Could not read the build version", e)
                null
            }
        }
    }

    private companion object {
        const val TAG = "AppInfoRepository"
    }
}
