package com.android.contacts.editornew.photo.picker

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.contacts.R
import com.android.contacts.editornew.photo.PhotoType
import com.android.contacts.ui.core.AppTheme

@Composable
internal fun PhotoSourceChooserDialog(
    type: PhotoType,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        PhotoSourceChooserDialogContent(
            type = type,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
        )
    }
}

@Composable
private fun PhotoSourceChooserDialogContent(
    type: PhotoType,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .testTag("contact_editor_photo_source_chooser_dialog_content")
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                text = stringResource(R.string.menu_change_photo),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SourceItem(
                text = stringResource(type.cameraStringRes),
                onClick = onCameraClick,
            )

            SourceItem(
                text = stringResource(type.galleryStringRes),
                onClick = onGalleryClick,
            )
        }
    }
}

@Composable
private fun SourceItem(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Text(
            text = text,
        )
    }
}

private val PhotoType.cameraStringRes
    @StringRes
    get() = when (this) {
        PhotoType.New -> R.string.take_photo
        PhotoType.Replace -> R.string.take_new_photo
    }

private val PhotoType.galleryStringRes
    @StringRes
    get() = when (this) {
        PhotoType.New -> R.string.pick_photo
        PhotoType.Replace -> R.string.pick_new_photo
    }

@Preview
@Composable
private fun PhotoSourceDialogContentPreview(
    @PreviewParameter(PhotoTypePreviewProvider::class)
    type: PhotoType,
) {
    AppTheme {
        PhotoSourceChooserDialogContent(
            type = type,
            onCameraClick = {},
            onGalleryClick = {},
        )
    }
}

private class PhotoTypePreviewProvider : PreviewParameterProvider<PhotoType> {
    override val values: Sequence<PhotoType> =
        PhotoType.entries.asSequence()
}
