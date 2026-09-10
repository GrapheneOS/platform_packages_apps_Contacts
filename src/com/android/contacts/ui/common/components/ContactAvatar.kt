package com.android.contacts.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.android.contacts.ui.core.ContactsPreviewColumn

private val PreviewAvatarSize = 96.dp

@Composable
internal fun ContactAvatar(
    avatarImage: ContactAvatarImage?,
    fallbackIcon: ImageVector,
    fallbackSize: Dp,
    fallbackLabel: String?,
    modifier: Modifier = Modifier,
    colorSeed: String? = null,
    shape: Shape = CircleShape,
    isSelected: Boolean = false,
) {
    val fallbackColors = resolvedFallbackColors(
        colorSeed = colorSeed,
        isSelected = isSelected,
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(fallbackColors.background),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isSelected -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(fallbackSize),
                    tint = fallbackColors.content,
                )
            }

            avatarImage != null -> {
                AsyncImage(
                    model = when (avatarImage) {
                        is ContactAvatarImage.Bytes -> avatarImage.value
                        is ContactAvatarImage.Uri -> avatarImage.value
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            !fallbackLabel.isNullOrBlank() -> {
                val fontSize = with(LocalDensity.current) { fallbackSize.toSp() }

                Text(
                    text = fallbackLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    color = fallbackColors.content,
                )
            }

            else -> {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    modifier = Modifier.size(fallbackSize),
                    tint = fallbackColors.content,
                )
            }
        }
    }
}

@Composable
internal fun ContactAvatar(
    avatarImage: ContactAvatarImage?,
    size: Dp,
    fallbackLabel: String?,
    modifier: Modifier = Modifier,
    colorSeed: String? = null,
    fallbackSize: Dp = size / 2,
    fallbackIcon: ImageVector = Icons.Rounded.Person,
    shape: Shape = CircleShape,
    isSelected: Boolean = false,
) {
    ContactAvatar(
        avatarImage = avatarImage,
        fallbackIcon = fallbackIcon,
        fallbackSize = fallbackSize,
        fallbackLabel = fallbackLabel,
        modifier = modifier.size(size),
        colorSeed = colorSeed,
        shape = shape,
        isSelected = isSelected,
    )
}

internal fun contactAvatarColorSeed(lookupKey: String?): String? {
    return lookupKey
        ?.filterNot { character -> character.isWhitespace() }
        ?.takeIf { key -> key.isNotEmpty() }
}

internal fun contactAvatarLabel(source: String?): String? {
    val trimmedSource = source
        ?.trim()
        ?.dropWhile(::isFormatChar)

    return when {
        trimmedSource.isNullOrBlank() -> null
        !trimmedSource.first().isLetter() -> null
        else -> trimmedSource.first().uppercaseChar().toString()
    }
}

private fun isFormatChar(char: Char): Boolean {
    return char.category == CharCategory.FORMAT
}

@PreviewLightDark
@Composable
private fun ContactAvatarPreview() {
    ContactsPreviewColumn {
        ContactAvatar(
            avatarImage = null,
            size = PreviewAvatarSize,
            fallbackLabel = contactAvatarLabel("Anna Smith"),
            colorSeed = contactAvatarColorSeed("anna"),
        )
        ContactAvatar(
            avatarImage = null,
            size = PreviewAvatarSize,
            fallbackLabel = contactAvatarLabel("+1 555 0001"),
            colorSeed = contactAvatarColorSeed("+15550001"),
        )
        ContactAvatar(
            avatarImage = null,
            size = PreviewAvatarSize,
            fallbackLabel = null,
            fallbackIcon = Icons.Rounded.Business,
        )
        ContactAvatar(
            avatarImage = null,
            size = PreviewAvatarSize,
            fallbackLabel = contactAvatarLabel("Anna Smith"),
            colorSeed = contactAvatarColorSeed("anna"),
            isSelected = true,
        )
    }
}
