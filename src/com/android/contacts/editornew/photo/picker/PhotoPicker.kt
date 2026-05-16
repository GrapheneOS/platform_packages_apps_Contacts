package com.android.contacts.editornew.photo.picker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.android.contacts.editornew.ContactEditorEvent.Photo
import com.android.contacts.editornew.ContactEditorUiState
import com.android.contacts.editornew.ContactEditorViewModel

@Composable
internal fun PhotoPicker(
    viewModel: ContactEditorViewModel,
    viewState: ContactEditorUiState,
) {
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(Photo.Choose::Result)
            ?.run(viewModel::onEvent)
    }

    val cropPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.onEvent(
            Photo.CropResult(
                uri = result.data?.data,
            ),
        )
    }

    LaunchedEffect(viewModel.photoEffects) {
        viewModel.photoEffects.collect { effect ->
            when (effect) {
                PhotoEffect.OpenCamera -> {
                    // TODO
                }

                PhotoEffect.OpenPhotoPicker -> {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                }

                is PhotoEffect.CropPhoto -> {
                    cropPhotoLauncher.launch(effect.intent)
                }
            }
        }
    }

    viewState.photoSourceDialogUiState?.let { photoSourceDialog ->
        PhotoSourceChooserDialog(
            type = photoSourceDialog.type,
            onDismiss = { viewModel.onEvent(Photo.Choose.Dismiss) },
            onCameraClick = { viewModel.onEvent(Photo.Choose.FromCameraClick) },
            onGalleryClick = { viewModel.onEvent(Photo.Choose.FromGalleryClick) },
        )
    }
}
