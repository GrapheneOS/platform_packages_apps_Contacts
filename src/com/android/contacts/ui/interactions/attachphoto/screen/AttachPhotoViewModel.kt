package com.android.contacts.ui.interactions.attachphoto.screen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.data.photo.model.TempPhotoUris
import com.android.contacts.data.photo.repository.ContactPhotoRepository
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.photo.model.AttachPhotoResult
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import com.android.contacts.domain.photo.usecase.AttachPhotoToContact
import com.android.contacts.domain.photo.usecase.GetPhotoCropSize
import com.android.contacts.domain.photo.usecase.ResolveAttachPhotoTarget
import com.android.contacts.ui.interactions.attachphoto.AttachPhotoActivity.Companion.EXTRA_MIME_TYPE
import com.android.contacts.ui.interactions.attachphoto.AttachPhotoActivity.Companion.EXTRA_SOURCE_URI
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoAction as Action
import com.android.contacts.ui.interactions.attachphoto.screen.model.AttachPhotoEffect as Effect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface AttachPhotoScreenModel {
    val effects: Flow<Effect>

    fun onAction(action: Action)
}

@HiltViewModel
internal class AttachPhotoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactPhotoRepository: ContactPhotoRepository,
    private val getPhotoCropSize: GetPhotoCropSize,
    private val resolveAttachPhotoTarget: ResolveAttachPhotoTarget,
    private val attachPhotoToContact: AttachPhotoToContact,
    private val getDefaultAccount: GetDefaultAccount,
) : ViewModel(),
    AttachPhotoScreenModel {

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    override val effects: Flow<Effect> = _effects.receiveAsFlow()

    private val sourcePhotoUri: Uri? = savedStateHandle[EXTRA_SOURCE_URI]
    private val sourcePhotoMimeType: String? = savedStateHandle[EXTRA_MIME_TYPE]

    private var tempPhotoUris: TempPhotoUris?
        get() = savedStateHandle[KEY_TEMP_PHOTO_URIS]
        set(value) = savedStateHandle.set(KEY_TEMP_PHOTO_URIS, value)

    private var contactUri: Uri?
        get() = savedStateHandle[KEY_CONTACT_URI]
        set(value) = savedStateHandle.set(KEY_CONTACT_URI, value)

    init {
        when {
            sourcePhotoUri == null -> {
                Log.w(TAG, "No photo to attach")
                closeWithFailure()
            }
            tempPhotoUris == null -> {
                viewModelScope.launch {
                    tempPhotoUris = contactPhotoRepository.generateTempPhotoUris()
                    emitEffect(Effect.PickContact)
                }
            }
        }
    }

    override fun onAction(action: Action) {
        when (action) {
            is Action.ContactPicked -> onContactPicked(action.contactUri)
            Action.CropUnavailable -> onCropUnavailable()
            is Action.CropFinished -> onCropFinished(action.isSuccess)
            Action.DefaultAccountPicked -> onDefaultAccountPicked()
        }
    }

    private fun onContactPicked(pickedContactUri: Uri?) {
        viewModelScope.launch {
            val source = sourcePhotoUri
            val uris = tempPhotoUris
            if (pickedContactUri == null || source == null || uris == null) {
                deleteTempPhotosAndCloseWithFailure()
                return@launch
            }
            contactUri = pickedContactUri

            // Save the URI into a temporary file provider URI so that
            // we can add the FLAG_GRANT_WRITE_URI_PERMISSION flag to the eventual
            // crop intent for read-only URI's.
            if (!contactPhotoRepository.copyPhoto(source, uris.temp)) {
                deleteTempPhotosAndCloseWithFailure()
                return@launch
            }
            emitEffect(
                Effect.CropPhoto(
                    inputUri = uris.temp,
                    outputUri = uris.cropped,
                    mimeType = sourcePhotoMimeType,
                    size = getPhotoCropSize(),
                ),
            )
        }
    }

    private fun onCropUnavailable() {
        viewModelScope.launch {
            val uris = tempPhotoUris ?: return@launch deleteTempPhotosAndCloseWithFailure()

            // When no crop activity is available, square crop the photo automatically
            val isSuccessful = contactPhotoRepository.cropToSquare(
                source = uris.temp,
                output = uris.cropped,
                size = getPhotoCropSize(),
            )
            wrapUpCrop(isSuccessful)
        }
    }

    private fun onCropFinished(isSuccessful: Boolean) {
        viewModelScope.launch {
            wrapUpCrop(isSuccessful)
        }
    }

    private suspend fun wrapUpCrop(isSuccessful: Boolean) {
        when {
            isSuccessful -> saveToContact()
            else -> deleteTempPhotosAndCloseWithFailure()
        }
    }

    private fun onDefaultAccountPicked() {
        viewModelScope.launch {
            val account = getDefaultAccount() ?: run {
                Log.w(TAG, "Account selector was not successful")
                deleteTempPhotosAndCloseWithFailure()
                return@launch
            }
            attach(AttachPhotoTarget.Ready.NewRawContact(account = account))
        }
    }

    private suspend fun saveToContact() {
        val contactUri = contactUri ?: return deleteTempPhotosAndCloseWithFailure()
        when (val target = resolveAttachPhotoTarget(contactUri)) {
            is AttachPhotoTarget.Ready -> {
                attach(target)
            }
            AttachPhotoTarget.NeedsAccountSelection -> {
                emitEffect(Effect.PickDefaultAccount)
            }
            AttachPhotoTarget.NotFound -> {
                Log.w(TAG, "No raw contacts found for contact")
                deleteTempPhotosAndCloseWithFailure()
            }
        }
    }

    private suspend fun attach(target: AttachPhotoTarget.Ready) {
        val contactUri = contactUri
        val uris = tempPhotoUris ?: return closeWithFailure()
        when {
            contactUri != null -> {
                val result = attachPhotoToContact(contactUri, target, uris.cropped)
                when (result) {
                    AttachPhotoResult.SUCCESS -> deleteTempPhotoAndCloseWithSuccess()
                    else -> deleteTempPhotosAndCloseWithFailure()
                }
            }
            else -> deleteTempPhotosAndCloseWithFailure()
        }
    }

    private suspend fun deleteTempPhotosAndCloseWithFailure() {
        tempPhotoUris?.let {
            contactPhotoRepository.deletePhoto(it.temp)
            contactPhotoRepository.deletePhoto(it.cropped)
        }
        closeWithFailure()
    }

    private fun closeWithFailure() {
        emitEffect(Effect.Close(isSuccess = false))
    }

    private suspend fun deleteTempPhotoAndCloseWithSuccess() {
        tempPhotoUris?.let {
            contactPhotoRepository.deletePhoto(it.temp)
            // the cropped file was taken over by the ContactSaveService
        }
        emitEffect(Effect.Close(isSuccess = true))
    }

    private fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    private companion object {
        const val TAG = "AttachPhotoViewModel"
        const val KEY_TEMP_PHOTO_URIS = "temp_photo_uris"
        const val KEY_CONTACT_URI = "contact_uri"
    }
}
