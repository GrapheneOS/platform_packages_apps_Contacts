package com.android.contacts.editornew.photo.picker

import android.net.Uri

internal data class PhotoPickerState(
    val photoUri: Uri?,
    val showPhotoActionChooserDialog: Boolean,
) {
    companion object {
        val DEFAULT = PhotoPickerState(
            photoUri = null,
            showPhotoActionChooserDialog = false,
        )
    }
}
