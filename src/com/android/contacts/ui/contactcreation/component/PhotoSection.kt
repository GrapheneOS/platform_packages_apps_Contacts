package com.android.contacts.ui.contactcreation.component

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

private const val AVATAR_SIZE_DP = 96
private const val PHOTO_DOWNSAMPLE_PX = 288 // 96dp * 3 (xxxhdpi)
private const val PLACEHOLDER_ICON_SIZE_DP = 48

internal fun LazyListScope.photoSection(
    photoUri: Uri?,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "photo_avatar", contentType = "photo_avatar") {
        PhotoAvatar(
            photoUri = photoUri,
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        )
    }
}

@Composable
internal fun PhotoAvatar(
    photoUri: Uri?,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box {
            AvatarSurface(photoUri = photoUri, onClick = { menuExpanded = true })
            PhotoDropdownMenu(
                expanded = menuExpanded,
                hasPhoto = photoUri != null,
                onDismiss = { menuExpanded = false },
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun AvatarSurface(photoUri: Uri?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(AVATAR_SIZE_DP.dp)
            .clip(CircleShape)
            .testTag(TestTags.PHOTO_AVATAR)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (photoUri != null) {
            PhotoImage(photoUri)
        } else {
            PlaceholderIcon()
        }
    }
}

@Composable
private fun PhotoImage(photoUri: Uri) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photoUri)
            .size(Size(PHOTO_DOWNSAMPLE_PX, PHOTO_DOWNSAMPLE_PX))
            .crossfade(true)
            .build(),
        contentDescription = "Contact photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(AVATAR_SIZE_DP.dp),
    )
}

@Composable
private fun PlaceholderIcon() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(AVATAR_SIZE_DP.dp)) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Add photo",
            modifier = Modifier
                .size(PLACEHOLDER_ICON_SIZE_DP.dp)
                .testTag(TestTags.PHOTO_PLACEHOLDER_ICON),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PhotoDropdownMenu(
    expanded: Boolean,
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onAction: (ContactCreationAction) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(TestTags.PHOTO_MENU),
    ) {
        DropdownMenuItem(
            text = { Text("Choose photo") },
            onClick = {
                onDismiss()
                onAction(ContactCreationAction.RequestGallery)
            },
            leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
            modifier = Modifier.testTag(TestTags.PHOTO_PICK_GALLERY),
        )
        DropdownMenuItem(
            text = { Text("Take photo") },
            onClick = {
                onDismiss()
                onAction(ContactCreationAction.RequestCamera)
            },
            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
            modifier = Modifier.testTag(TestTags.PHOTO_TAKE_CAMERA),
        )
        if (hasPhoto) {
            DropdownMenuItem(
                text = { Text("Remove photo") },
                onClick = {
                    onDismiss()
                    onAction(ContactCreationAction.RemovePhoto)
                },
                leadingIcon = { Icon(Icons.Filled.Close, contentDescription = null) },
                modifier = Modifier.testTag(TestTags.PHOTO_REMOVE),
            )
        }
    }
}
