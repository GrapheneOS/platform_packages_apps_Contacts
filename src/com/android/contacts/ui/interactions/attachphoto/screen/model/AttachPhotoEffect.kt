package com.android.contacts.ui.interactions.attachphoto.screen.model

import android.net.Uri

internal sealed interface AttachPhotoEffect {

    data object PickContact : AttachPhotoEffect

    data class CropPhoto(
        val inputUri: Uri,
        val outputUri: Uri,
        val mimeType: String?,
        val size: Int,
    ) : AttachPhotoEffect

    data object PickDefaultAccount : AttachPhotoEffect

    /** Finishes the activity with [isSuccess] reported as RESULT_OK or RESULT_CANCELED. */
    data class Close(
        val isSuccess: Boolean,
    ) : AttachPhotoEffect
}
