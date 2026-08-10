package com.android.contacts.data.intents

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import javax.inject.Inject

internal fun interface GetIntentLabel {
    operator fun invoke(intent: Intent): String?
}

internal class GetIntentLabelImpl @Inject constructor(
    private val packageManager: PackageManager,
) : GetIntentLabel {

    override operator fun invoke(intent: Intent): String? {
        val matches = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        if (matches.isEmpty()) {
            return null
        }

        return bestMatch(intent, matches)
            .loadLabel(packageManager)
            .toString()
    }

    private fun bestMatch(
        intent: Intent,
        matches: List<ResolveInfo>,
    ): ResolveInfo {
        if (matches.size == 1) {
            return matches.first()
        }

        val defaultMatch = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return when {
            defaultMatch != null && !isDisambiguation(defaultMatch) -> defaultMatch
            else -> preferredMatch(matches)
        }
    }

    private fun preferredMatch(matches: List<ResolveInfo>): ResolveInfo {
        return matches.firstOrNull(::isPreferredPackage)
            ?: matches.firstOrNull(::isSystemApplication)
            ?: matches.first()
    }

    private fun isDisambiguation(resolveInfo: ResolveInfo): Boolean {
        return resolveInfo.match and IntentFilter.MATCH_CATEGORY_MASK == 0
    }

    private fun isPreferredPackage(resolveInfo: ResolveInfo): Boolean {
        return resolveInfo.activityInfo.applicationInfo.packageName in PREFERRED_PACKAGES
    }

    private fun isSystemApplication(resolveInfo: ResolveInfo): Boolean {
        val flags = resolveInfo.activityInfo.applicationInfo.flags

        return flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    private companion object {
        val PREFERRED_PACKAGES = setOf(
            "com.android.browser",
            "com.android.chrome",
            "com.android.email",
            "com.android.phone",
            "com.google.android.apps.maps",
            "com.google.android.browser",
            "com.google.android.email",
            "org.chromium.webview_shell",
        )
    }
}
