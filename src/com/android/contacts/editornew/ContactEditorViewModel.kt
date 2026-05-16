package com.android.contacts.editornew

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Photo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.contacts.editor.EditorUiUtils
import com.android.contacts.editornew.contact.ContactDelegate
import com.android.contacts.editornew.contact.ContactState
import com.android.contacts.editornew.photo.PhotoType
import com.android.contacts.editornew.photo.picker.PhotoDelegate
import com.android.contacts.editornew.photo.picker.PhotoPickerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class ContactEditorViewModel
@Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val photoDelegate: PhotoDelegate,
    private val contactDelegate: ContactDelegate,
) : ViewModel(),
    PhotoDelegate by photoDelegate {

    private val _contactEditorEffects =
        MutableSharedFlow<ContactEditorEffect>(extraBufferCapacity = 1)
    val contactEditorEffects: Flow<ContactEditorEffect> = _contactEditorEffects.asSharedFlow()

    val uiState: StateFlow<ContactEditorUiState> = photoDelegate.state.map { photoState ->
        val photoSourceDialogUiState = if (photoState.showPhotoActionChooserDialog) {
            ContactEditorUiState.PhotoSourceDialogUiState(
                type = if (photoState.photoUri != null) PhotoType.Replace else PhotoType.New,
            )
        } else {
            null
        }

        ContactEditorUiState(
            photoUiState = photoState.toUiState(),
            photoSourceDialogUiState = photoSourceDialogUiState,
        )
    }
        .onStart { contactDelegate.init() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ContactEditorUiState.DEFAULT,
        )

    fun onEvent(event: ContactEditorEvent) {
        when (event) {
            ContactEditorEvent.Save -> save()
            is ContactEditorEvent.Photo -> photoDelegate.onEvent(viewModelScope, event)
        }
    }

    /**
     * Save new contact; Right now this is very simplified, only the picture will be saved.
     */
    private fun save() {
        val photoUri = photoDelegate.state.value.photoUri ?: return
        val contactState = contactDelegate.state.value

        when (contactState) {
            ContactState.Loading -> return
            is ContactState.Data -> Unit
        }

        val rawContactDelta = contactState.rawContactDelta

        // Save thumbnail, this simplifies code, because we don't have to test whether
        // there is a change in either the delta-list or a changed photo, this way,
        // there is always a change in the delta-list.
        rawContactDelta
            .getSuperPrimaryEntry(Photo.CONTENT_ITEM_TYPE)
            .photo = EditorUiUtils.getCompressedThumbnailBitmapBytes(context, photoUri)

        val updatedPhotos = Bundle().apply {
            putParcelable(rawContactDelta.rawContactId.toString(), photoUri)
        }

        emitEffect(
            ContactEditorEffect.Save(
                rawContactDelta = rawContactDelta,
                updatedPhotos = updatedPhotos,
            ),
        )
    }

    private fun emitEffect(effect: ContactEditorEffect) {
        viewModelScope.launch {
            _contactEditorEffects.emit(effect)
        }
    }

    private fun PhotoPickerState.toUiState() = photoUri
        ?.let(ContactEditorUiState.PhotoUiState::Photo)
        ?: ContactEditorUiState.PhotoUiState.Placeholder
}
