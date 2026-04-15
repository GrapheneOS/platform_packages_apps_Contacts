package com.android.contacts.ui.contactcreation.model

import android.net.Uri
import com.android.contacts.ui.contactcreation.mapper.DeltaMapperResult

internal sealed interface ContactCreationEffect {
    data class Save(val result: DeltaMapperResult) : ContactCreationEffect
    data class SaveSuccess(val contactUri: Uri?) : ContactCreationEffect
    data class ShowError(val messageResId: Int) : ContactCreationEffect
    data object NavigateBack : ContactCreationEffect
    data object LaunchGallery : ContactCreationEffect
    data class LaunchCamera(val outputUri: Uri) : ContactCreationEffect
}
