package com.android.contacts.editornew.photo

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.contacts.R
import com.android.contacts.editornew.ContactEditorUiState
import com.android.contacts.ui.core.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ContactEditorPhoto(
    viewState: ContactEditorUiState.PhotoUiState,
    onAddOrChangeClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape),
            targetState = viewState,
        ) { targetViewState ->
            when (targetViewState) {
                is ContactEditorUiState.PhotoUiState.Photo -> {
                    Photo(
                        uri = targetViewState.uri,
                        onClick = onAddOrChangeClick,
                    )
                }
                ContactEditorUiState.PhotoUiState.Placeholder -> {
                    Placeholder(
                        onClick = onAddOrChangeClick,
                    )
                }
            }
        }

        AnimatedContent(
            targetState = viewState,
        ) { targetViewState ->
            when (targetViewState) {
                is ContactEditorUiState.PhotoUiState.Photo -> {
                    Row {
                        ActionButton(
                            onClick = onAddOrChangeClick,
                            icon = Icons.Default.Edit,
                            text = stringResource(R.string.contact_editor_photo_change),
                        )

                        ActionButton(
                            onClick = onRemoveClick,
                            icon = Icons.Default.Delete,
                            text = stringResource(R.string.contact_editor_photo_remove),
                        )
                    }
                }
                ContactEditorUiState.PhotoUiState.Placeholder -> {
                    ActionButton(
                        onClick = onAddOrChangeClick,
                        icon = null,
                        text = stringResource(R.string.contact_editor_photo_add),
                    )
                }
            }
        }
    }
}

@Composable
private fun Placeholder(
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .testTag("contact_editor_photo_placeholder"),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(56.dp),
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = stringResource(R.string.editor_add_photo_content_description),
        )
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector?,
    text: String,
) {
    TextButton(onClick = onClick) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Decorative purpose.
            )

            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(text = text)
    }
}

@Composable
private fun Photo(
    onClick: () -> Unit,
    uri: Uri,
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        imageBitmap = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory
                    .decodeStream(inputStream)
                    ?.asImageBitmap()
            }
        }
    }

    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = stringResource(R.string.editor_contact_photo_content_description),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview
@Composable
internal fun ContactEditorPhotoPreview(
    @PreviewParameter(ContactEditorPhotoPreviewProvider::class)
    viewState: ContactEditorUiState.PhotoUiState,
) {
    AppTheme {
        ContactEditorPhoto(
            viewState = viewState,
            onAddOrChangeClick = {},
            onRemoveClick = {},
        )
    }
}

internal class ContactEditorPhotoPreviewProvider :
    PreviewParameterProvider<ContactEditorUiState.PhotoUiState> {
    override val values: Sequence<ContactEditorUiState.PhotoUiState> = sequenceOf(
        ContactEditorUiState.PhotoUiState.Photo(uri = "test".toUri()),
        ContactEditorUiState.PhotoUiState.Placeholder,
    )
}
