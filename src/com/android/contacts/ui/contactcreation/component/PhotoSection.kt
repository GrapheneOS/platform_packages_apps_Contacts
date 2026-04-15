@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.contactcreation.component

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

private const val AVATAR_SIZE_DP = 120
private const val PHOTO_DOWNSAMPLE_PX = 360 // 120dp * 3 (xxxhdpi)
private const val PLACEHOLDER_ICON_SIZE_DP = 56
private const val MORPHED_CORNER_DP = 20
private const val BG_STRIP_HEIGHT_DP = 168
private const val CAMERA_BADGE_SIZE_DP = 32
private const val CAMERA_BADGE_ICON_SIZE_DP = 16

/**
 * Photo section as a @Composable (for Column-based layout).
 * 120dp circle centered on plain surface. Tap opens ModalBottomSheet.
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
    var showSheet by remember { mutableStateOf(false) }
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
                        showSheet = true
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
            // Camera badge at bottom-right
            Surface(
                modifier = Modifier
                    .size(CAMERA_BADGE_SIZE_DP.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(CAMERA_BADGE_ICON_SIZE_DP.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }

    if (showSheet) {
        PhotoBottomSheet(
            hasPhoto = photoUri != null,
            onAction = onAction,
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun PhotoBottomSheet(
    hasPhoto: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissAndDo(action: ContactCreationAction) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                onAction(action)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag(TestTags.PHOTO_MENU),
    ) {
        Text(
            text = stringResource(R.string.contact_creation_contact_photo),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.take_photo)) },
            leadingContent = {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
            },
            modifier = Modifier
                .clickable { dismissAndDo(ContactCreationAction.RequestCamera) }
                .testTag(TestTags.PHOTO_TAKE_CAMERA),
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.contact_creation_choose_photo)) },
            leadingContent = {
                Icon(Icons.Filled.Image, contentDescription = null)
            },
            modifier = Modifier
                .clickable { dismissAndDo(ContactCreationAction.RequestGallery) }
                .testTag(TestTags.PHOTO_PICK_GALLERY),
        )
        if (hasPhoto) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.removePhoto)) },
                leadingContent = {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                },
                modifier = Modifier
                    .clickable { dismissAndDo(ContactCreationAction.RemovePhoto) }
                    .testTag(TestTags.PHOTO_REMOVE),
            )
        }
        Spacer(Modifier.navigationBarsPadding())
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
