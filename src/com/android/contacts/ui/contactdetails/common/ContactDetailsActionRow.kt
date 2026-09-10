package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.ui.common.components.TwoLineListItem
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun ContactDetailsActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isFirst: Boolean = true,
    isLast: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    TwoLineListItem(
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .padding(
                        start = Tokens.actionRowLeadingPadding,
                        top = Tokens.actionRowExtraVerticalPadding,
                        bottom = Tokens.actionRowExtraVerticalPadding,
                    )
                    .size(Tokens.rowIconSize),
            )
        },
        titleContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        trailingContent = trailingContent,
        shape = cellShape(
            isFirst = isFirst,
            isLast = isLast,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        subtitleContent = when (subtitle) {
            null -> null

            else -> {
                {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ContactDetailsActionRowPreview() {
    ContactsPreviewColumn {
        ContactDetailsActionRow(
            icon = Icons.Rounded.Notifications,
            title = "Set ringtone",
            subtitle = "Bright Morning",
            onClick = {},
        )
    }
}
