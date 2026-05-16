package com.android.contacts.editornew

import android.net.Uri
import com.android.contacts.editornew.photo.PhotoType

internal data class ContactEditorUiState(
    val photoUiState: PhotoUiState,
    val photoSourceDialogUiState: PhotoSourceDialogUiState?,
) {
    companion object {
        val DEFAULT = ContactEditorUiState(
            photoUiState = PhotoUiState.Placeholder,
            photoSourceDialogUiState = null,
        )
    }

    sealed interface PhotoUiState {
        data class Photo(val uri: Uri) : PhotoUiState
        data object Placeholder : PhotoUiState
    }

    data class PhotoSourceDialogUiState(val type: PhotoType)
}
