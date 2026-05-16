package com.android.contacts.editornew.photo.picker

import android.content.Intent

internal sealed interface PhotoEffect {
    data object OpenPhotoPicker : PhotoEffect
    data object OpenCamera : PhotoEffect
    data class CropPhoto(val intent: Intent) : PhotoEffect
}
