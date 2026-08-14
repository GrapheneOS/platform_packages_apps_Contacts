package com.android.contacts.ui.settings.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.core.ContactsPreviewColumn

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SettingsCell(
    title: String,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = cellShape(
        isFirst = isFirst,
        isLast = isLast,
    )

    val interactionModifier = when {
        onClick != null -> {
            Modifier
                .clip(shape)
                .combinedClickable(
                    onLongClick = onLongClick,
                    onLongClickLabel = onLongClickLabel,
                    onClick = onClick,
                )
        }

        onLongClick != null -> {
            Modifier.longPressable(
                shape = shape,
                label = onLongClickLabel,
                interactionSource = interactionSource,
                onLongClick = onLongClick,
            )
        }

        else -> Modifier.semantics(mergeDescendants = true) {}
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape,
        modifier = modifier.then(interactionModifier),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

private fun Modifier.longPressable(
    shape: Shape,
    label: String?,
    interactionSource: MutableInteractionSource,
    onLongClick: () -> Unit,
): Modifier {
    return this
        .clip(shape)
        .indication(interactionSource, ripple())
        .pointerInput(onLongClick) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interactionSource.emit(press)

                    val isReleased = tryAwaitRelease()
                    interactionSource.emit(
                        when {
                            isReleased -> PressInteraction.Release(press)
                            else -> PressInteraction.Cancel(press)
                        },
                    )
                },
                onLongPress = { onLongClick() },
            )
        }
        .semantics(mergeDescendants = true) {
            onLongClick(label = label) {
                onLongClick()
                true
            }
        }
}

@Composable
private fun cellShape(
    isFirst: Boolean,
    isLast: Boolean,
): Shape {
    val cornerSize = MaterialTheme.shapes.extraSmall.topStart
    val defaultCornerSize = CornerSize(2.dp)

    return RoundedCornerShape(
        topStart = if (isFirst) cornerSize else defaultCornerSize,
        topEnd = if (isFirst) cornerSize else defaultCornerSize,
        bottomStart = if (isLast) cornerSize else defaultCornerSize,
        bottomEnd = if (isLast) cornerSize else defaultCornerSize,
    )
}

@PreviewLightDark
@Composable
private fun SettingsCellGroupPreview() {
    ContactsPreviewColumn {
        SettingsCell(
            title = "My info",
            summary = "Anna Smith",
            isFirst = true,
            isLast = false,
            onClick = {},
        )
        SettingsCell(
            title = "Accounts",
            isFirst = false,
            isLast = false,
            onClick = {},
        )
        SettingsCell(
            title = "Default account for new contacts",
            summary = "Device",
            isFirst = false,
            isLast = true,
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsCellSinglePreview() {
    ContactsPreviewColumn {
        SettingsCell(
            title = "About Contacts",
            isFirst = true,
            isLast = true,
            onClick = {},
        )
        SettingsCell(
            title = "Build version",
            summary = "1.0.0",
            isFirst = true,
            isLast = true,
        )
    }
}
