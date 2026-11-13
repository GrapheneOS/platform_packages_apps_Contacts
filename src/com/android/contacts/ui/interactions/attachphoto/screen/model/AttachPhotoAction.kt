package com.android.contacts.ui.interactions.attachphoto.screen.model

import android.net.Uri

internal sealed interface AttachPhotoAction {

    data class ContactPicked(
        val contactUri: Uri?,
    ) : AttachPhotoAction

    data object CropUnavailable : AttachPhotoAction

    data class CropFinished(
        val isSuccess: Boolean,
    ) : AttachPhotoAction

    data object DefaultAccountPicked : AttachPhotoAction
}
