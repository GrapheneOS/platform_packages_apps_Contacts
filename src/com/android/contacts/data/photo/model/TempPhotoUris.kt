package com.android.contacts.data.photo.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class TempPhotoUris(
    val temp: Uri,
    val cropped: Uri,
) : Parcelable
