package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallMissed
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.ui.common.components.TwoLineListItem
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.RecentCallDirection
import com.android.contacts.ui.contactdetails.screen.model.RecentCallUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun ContactDetailsRecentCallRow(
    recentCall: RecentCallUiModel,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TwoLineListItem(
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = recentCall.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier.semantics {
            contentDescription = recentCall.contentDescription
        },
        shape = cellShape(
            isFirst = false,
            isLast = isLast,
        ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        subtitleContent = {
            RecentCallSubtitle(recentCall = recentCall)
        },
        trailingContent = {
            Text(
                text = recentCall.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.clearAndSetSemantics {},
            )
        },
    )
}

@Composable
private fun RecentCallSubtitle(recentCall: RecentCallUiModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Tokens.recentCallDirectionSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = recentCall.direction.imageVector(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Tokens.recentCallDirectionSize),
        )

        val numberLabel = recentCall.numberLabel
        if (numberLabel != null) {
            Text(
                text = numberLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun RecentCallDirection.imageVector(): ImageVector {
    return when (this) {
        RecentCallDirection.INCOMING -> Icons.AutoMirrored.Rounded.CallReceived
        RecentCallDirection.OUTGOING -> Icons.AutoMirrored.Rounded.CallMade
        RecentCallDirection.MISSED -> Icons.AutoMirrored.Rounded.CallMissed
        RecentCallDirection.VOICEMAIL -> Icons.Rounded.Voicemail
        RecentCallDirection.REJECTED -> Icons.Rounded.PhoneDisabled
        RecentCallDirection.BLOCKED -> Icons.Rounded.Block
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsRecentCallRowPreview() {
    ContactsPreviewColumn {
        ContactDetailsRecentCallRow(
            recentCall = RecentCallUiModel(
                title = "Call time 01:20",
                numberLabel = "Mobile",
                date = "Jun 3",
                direction = RecentCallDirection.INCOMING,
                contentDescription = "Incoming call, 01:20, Jun 3",
            ),
            isLast = true,
            onClick = {},
        )
    }
}
