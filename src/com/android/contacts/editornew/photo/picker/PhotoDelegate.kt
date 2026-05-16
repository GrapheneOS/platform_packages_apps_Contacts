package com.android.contacts.editornew.photo.picker

import android.content.Intent
import android.net.Uri
import com.android.contacts.editornew.ContactEditorEvent
import com.android.contacts.util.ContactPhotoUtils
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface PhotoDelegate {
    val photoEffects: Flow<PhotoEffect>
    val state: StateFlow<PhotoPickerState>
    fun onEvent(scope: CoroutineScope, event: ContactEditorEvent.Photo)
}

internal class PhotoDelegateImpl
@Inject constructor(private val helper: PhotoDelegateHelper) :
    PhotoDelegate {

    private val _photoEffects = MutableSharedFlow<PhotoEffect>(extraBufferCapacity = 1)
    override val photoEffects: Flow<PhotoEffect> = _photoEffects.asSharedFlow()

    private val _state = MutableStateFlow(PhotoPickerState.DEFAULT)
    override val state: StateFlow<PhotoPickerState> = _state.asStateFlow()

    private val photoPickDimension = lazy {
        helper.photoPickDimension()
    }

    private val tmpPhotoUri = helper.tmpPhotoUri()

    override fun onEvent(scope: CoroutineScope, event: ContactEditorEvent.Photo) {
        when (event) {
            ContactEditorEvent.Photo.RemoveClick -> setPhotoUri(uri = null)
            ContactEditorEvent.Photo.AddOrChangeClick -> {
                setShowPhotoSourceChooserDialog(show = true)
            }
            is ContactEditorEvent.Photo.Choose -> {
                setShowPhotoSourceChooserDialog(show = false)

                when (event) {
                    ContactEditorEvent.Photo.Choose.Dismiss -> Unit
                    ContactEditorEvent.Photo.Choose.FromCameraClick -> Unit // TODO
                    ContactEditorEvent.Photo.Choose.FromGalleryClick -> {
                        emitEffect(scope, PhotoEffect.OpenPhotoPicker)
                    }
                    is ContactEditorEvent.Photo.Choose.Result -> {
                        handlePhotoResult(scope = scope, uri = event.uri)
                    }
                }
            }

            is ContactEditorEvent.Photo.CropResult -> handleCropResult(uri = event.uri)
        }
    }

    private fun handleCropResult(uri: Uri?) {
        helper.deleteTemporaryPhoto(tmpPhotoUri)
        if (uri != null) {
            setPhotoUri(uri = uri)
        }
    }

    private fun handlePhotoResult(scope: CoroutineScope, uri: Uri) {
        val inputUri = helper.uriToWriteableTmpImageUri(uri) ?: return

        val cropIntent = cropImageIntent(
            inputUri = inputUri,
            outputUri = helper.tmpCroppedPhotoUri(),
        )

        val intentHandler = helper.intentHandlerOrNull(cropIntent)
        if (intentHandler == null) {
            setPhotoUri(uri = inputUri)
        } else {
            val effect = cropIntent
                .apply { setPackage(intentHandler.activityInfo.packageName) }
                .let(PhotoEffect::CropPhoto)

            emitEffect(scope, effect)
        }
    }

    private fun emitEffect(scope: CoroutineScope, effect: PhotoEffect) {
        scope.launch {
            _photoEffects.emit(effect)
        }
    }

    private fun setShowPhotoSourceChooserDialog(show: Boolean) {
        _state.update { state ->
            state.copy(
                showPhotoActionChooserDialog = show,
            )
        }
    }

    private fun setPhotoUri(uri: Uri?) {
        _state.update { state ->
            state.copy(
                photoUri = uri,
            )
        }
    }

    private fun cropImageIntent(inputUri: Uri, outputUri: Uri): Intent {
        return Intent("com.android.camera.action.CROP")
            .apply { setDataAndType(inputUri, "image/*") }
            .also { intent ->
                ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri)
                ContactPhotoUtils.addCropExtras(intent, photoPickDimension.value)
            }
    }
}
