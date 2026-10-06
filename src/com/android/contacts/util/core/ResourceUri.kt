package com.android.contacts.util.core

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri

internal fun resourceUri(
    packageName: String,
    resourceId: Int,
): String? {
    if (resourceId == Resources.ID_NULL) {
        return null
    }

    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(packageName)
        .appendPath(resourceId.toString())
        .build()
        .toString()
}
