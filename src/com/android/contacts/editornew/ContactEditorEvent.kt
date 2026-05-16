package com.android.contacts.editornew

import android.net.Uri

internal sealed interface ContactEditorEvent {
    data object Save : ContactEditorEvent

    sealed interface Photo : ContactEditorEvent {
        data object AddOrChangeClick : Photo

        sealed interface Choose : Photo {
            data object Dismiss : Choose
            data object FromCameraClick : Choose
            data object FromGalleryClick : Choose
            data class Result(val uri: Uri) : Choose
        }

        data object RemoveClick : Photo
        data class CropResult(val uri: Uri?) : Photo
    }
}
