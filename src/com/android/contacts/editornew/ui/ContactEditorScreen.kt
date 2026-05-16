package com.android.contacts.editornew.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.editornew.ContactEditorEvent
import com.android.contacts.editornew.ContactEditorUiState
import com.android.contacts.editornew.photo.ContactEditorPhoto
import com.android.contacts.ui.core.AppTheme

@Composable
internal fun ContactEditorScreen(
    onEvent: (ContactEditorEvent) -> Unit,
    onBack: () -> Unit,
    uiState: ContactEditorUiState,
) {
    Scaffold(
        topBar = {
            ContactEditorTopAppBar(
                title = stringResource(R.string.contact_editor_create_contact),
                onNavigateBack = onBack,
                onSave = { onEvent(ContactEditorEvent.Save) },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
        ) {
            item(key = "contact_editor_photo") {
                ContactEditorPhoto(
                    viewState = uiState.photoUiState,
                    onAddOrChangeClick = {
                        onEvent(ContactEditorEvent.Photo.AddOrChangeClick)
                    },
                    onRemoveClick = {
                        onEvent(ContactEditorEvent.Photo.RemoveClick)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ContactEditorScreenPreview() {
    AppTheme {
        ContactEditorScreen(
            uiState = ContactEditorUiState.DEFAULT,
            onEvent = {},
            onBack = {},
        )
    }
}
