package com.android.contacts.ui.settings.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun SettingsCell(
    title: String,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = cellShape(
        isFirst = isFirst,
        isLast = isLast,
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape,
        modifier = when (onClick) {
            null -> modifier.semantics(mergeDescendants = true) {}
            else -> modifier.clip(shape).clickable(onClick = onClick)
        },
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
