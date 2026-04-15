package com.android.contacts.ui.contactcreation.component

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

private const val AVATAR_SIZE_DP = 120
private const val PHOTO_DOWNSAMPLE_PX = 360 // 120dp * 3 (xxxhdpi)
private const val PLACEHOLDER_ICON_SIZE_DP = 56
private const val MORPHED_CORNER_DP = 20
private const val BG_STRIP_HEIGHT_DP = 168

/**
 * Photo section as a @Composable (for Column-based layout).
 * 120dp circle centered in a surfaceContainerLow background strip.
 */
@Composable
internal fun PhotoSectionContent(
    photoUri: Uri?,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PhotoAvatar(
        photoUri = photoUri,
        onAction = onAction,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
internal fun PhotoAvatar(
    photoUri: Uri?,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) MORPHED_CORNER_DP.dp else (AVATAR_SIZE_DP / 2).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "avatar_shape_morph",
    )
    val morphedShape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .height(BG_STRIP_HEIGHT_DP.dp)
            .testTag(TestTags.PHOTO_BG_STRIP),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .clip(morphedShape)
                    .testTag(TestTags.PHOTO_AVATAR)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        menuExpanded = true
                    },
                shape = morphedShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                if (photoUri != null) {
                    PhotoImage(photoUri)
                } else {
                    PlaceholderIcon()
                }
            }
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
private fun PhotoImage(photoUri: Uri) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photoUri)
            .size(Size(PHOTO_DOWNSAMPLE_PX, PHOTO_DOWNSAMPLE_PX))
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.contact_creation_contact_photo),
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(AVATAR_SIZE_DP.dp),
    )
}

@Composable
private fun PlaceholderIcon() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(AVATAR_SIZE_DP.dp)) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = stringResource(R.string.contact_creation_add_photo),
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
            text = { Text(stringResource(R.string.contact_creation_choose_photo)) },
            onClick = {
                onDismiss()
                onAction(ContactCreationAction.RequestGallery)
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = stringResource(R.string.contact_creation_choose_photo),
                )
            },
            modifier = Modifier.testTag(TestTags.PHOTO_PICK_GALLERY),
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.take_photo)) },
            onClick = {
                onDismiss()
                onAction(ContactCreationAction.RequestCamera)
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.take_photo),
                )
            },
            modifier = Modifier.testTag(TestTags.PHOTO_TAKE_CAMERA),
        )
        if (hasPhoto) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.removePhoto)) },
                onClick = {
                    onDismiss()
                    onAction(ContactCreationAction.RemovePhoto)
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.removePhoto),
                    )
                },
                modifier = Modifier.testTag(TestTags.PHOTO_REMOVE),
            )
        }
    }
}
