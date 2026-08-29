package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import coil3.compose.AsyncImage
import com.android.contacts.R
import com.android.contacts.ui.common.components.TwoLineListItem
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.ContactConnectedAppUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContactDetailsConnectedAppRow(
    connectedApp: ContactConnectedAppUiModel,
    isExpanded: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TwoLineListItem(
        onClick = onClick,
        leadingContent = {
            ConnectedAppIcon(iconUri = connectedApp.iconUri)
        },
        titleContent = {
            Text(
                text = connectedApp.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            ConnectedAppToggle(isExpanded = isExpanded)
        },
        shape = cellShape(
            isFirst = false,
            isLast = isLast,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
    )
}

@Composable
private fun ConnectedAppToggle(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val icon = when {
        isExpanded -> Icons.Rounded.ExpandLess
        else -> Icons.Rounded.ExpandMore
    }
    val descriptionResource = when {
        isExpanded -> R.string.contact_details_connected_app_collapse
        else -> R.string.contact_details_connected_app_expand
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(end = Tokens.rowActionEndPadding)
            .size(
                width = Tokens.connectedAppToggleWidth,
                height = Tokens.connectedAppToggleHeight,
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = CircleShape,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(descriptionResource),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Tokens.rowIconSize),
        )
    }
}

@Composable
private fun ConnectedAppIcon(
    iconUri: String?,
    modifier: Modifier = Modifier,
) {
    val iconModifier = modifier
        .padding(
            start = Tokens.actionRowLeadingPadding,
            top = Tokens.actionRowExtraVerticalPadding,
            bottom = Tokens.actionRowExtraVerticalPadding,
        )
        .size(Tokens.rowIconSize)

    when (iconUri) {
        null -> {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = iconModifier,
            )
        }

        else -> {
            AsyncImage(
                model = iconUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = iconModifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsConnectedAppRowPreview() {
    ContactsPreviewColumn {
        ContactDetailsConnectedAppRow(
            connectedApp = previewConnectedApp(),
            isExpanded = false,
            isLast = false,
            onClick = {},
        )
        ContactDetailsConnectedAppRow(
            connectedApp = previewConnectedApp(),
            isExpanded = true,
            isLast = true,
            onClick = {},
        )
    }
}

private fun previewConnectedApp(): ContactConnectedAppUiModel {
    return ContactConnectedAppUiModel(
        packageName = "com.example.chat",
        label = "Chat",
        iconUri = null,
        entries = persistentListOf(),
    )
}
