package com.android.contacts.editornew

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.ContactSaveService
import com.android.contacts.activities.ContactEditorActivity
import com.android.contacts.activities.ContactEditorActivity.ContactEditor.SaveMode
import com.android.contacts.editor.ContactEditorFragment.JOIN_CONTACT_ID_EXTRA_KEY
import com.android.contacts.editor.ContactEditorFragment.SAVE_MODE_EXTRA_KEY
import com.android.contacts.editornew.photo.picker.PhotoPicker
import com.android.contacts.editornew.ui.ContactEditorScreen
import com.android.contacts.model.RawContactDeltaList

@Composable
internal fun ContactEditor(
    onNavigateBack: (() -> Unit),
    viewModel: ContactEditorViewModel = viewModel<ContactEditorViewModel>(),
) {
    val viewState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = LocalActivity.current

    LaunchedEffect(viewModel) {
        viewModel.contactEditorEffects.collect { effect ->
            when (effect) {
                is ContactEditorEffect.Save -> effect.saveContact(context, activity)
            }
        }
    }

    PhotoPicker(viewModel, viewState)

    ContactEditorScreen(
        onEvent = viewModel::onEvent,
        onBack = onNavigateBack,
        uiState = viewState,
    )
}

private fun ContactEditorEffect.Save.saveContact(
    context: Context,
    activity: Activity?,
) {
    ContactSaveService.startService(
        context,
        this.toCreateSaveContactIntent(context, activity),
        SaveMode.CLOSE,
    )
}

private fun ContactEditorEffect.Save.toCreateSaveContactIntent(
    context: Context,
    activity: Activity?,
): Intent = ContactSaveService.createSaveContactIntent(
    context,
    RawContactDeltaList().apply { add(rawContactDelta) },
    SAVE_MODE_EXTRA_KEY,
    SaveMode.CLOSE,
    false,
    activity?.javaClass,
    ContactEditorActivity.ACTION_SAVE_COMPLETED,
    updatedPhotos,
    JOIN_CONTACT_ID_EXTRA_KEY,
    null,
)
