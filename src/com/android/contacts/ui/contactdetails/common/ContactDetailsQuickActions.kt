package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.domain.contactdetails.model.ContactEntryAction as Action
import com.android.contacts.ui.common.components.MarqueeText
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val DISABLED_CONTENT_ALPHA = 0.4f

@Composable
internal fun ContactDetailsQuickActions(
    quickActions: ImmutableList<ContactQuickActionUiModel>,
    onActionClick: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Tokens.quickActionSpacing),
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG),
    ) {
        quickActions.forEach { quickAction ->
            QuickAction(
                quickAction = quickAction,
                onClick = { quickAction.action?.let(onActionClick) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickAction(
    quickAction: ContactQuickActionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnabled = quickAction.action != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Surface(
            onClick = onClick,
            enabled = isEnabled,
            shape = CircleShape,
            color = quickActionColor(isEnabled),
            contentColor = quickActionContentColor(isEnabled),
            modifier = Modifier
                .fillMaxWidth()
                .height(Tokens.quickActionHeight)
                .testTag(CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX + quickAction.icon.name)
                .semantics { contentDescription = quickAction.label },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = quickAction.icon.imageVector(),
                    contentDescription = null,
                    modifier = Modifier.size(Tokens.quickActionIconSize),
                )
            }
        }

        Spacer(modifier = Modifier.height(Tokens.quickActionLabelSpacing))

        MarqueeText(
            text = quickAction.label,
            style = MaterialTheme.typography.labelLarge,
            color = quickActionLabelColor(isEnabled),
            fadeEdgeWidth = Tokens.quickActionLabelFadeWidth,
            contentAlignment = Alignment.Center,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun quickActionColor(isEnabled: Boolean): Color {
    return when {
        isEnabled -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
}

@Composable
private fun quickActionContentColor(isEnabled: Boolean): Color {
    return when {
        isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }
}

@Composable
private fun quickActionLabelColor(isEnabled: Boolean): Color {
    return when {
        isEnabled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsQuickActionsPreview() {
    ContactsPreviewColumn {
        ContactDetailsQuickActions(
            quickActions = persistentListOf(
                ContactQuickActionUiModel(
                    icon = ContactEntryIcon.CALL,
                    label = "Call",
                    action = Action.Call("088 525 7470"),
                ),
                ContactQuickActionUiModel(
                    icon = ContactEntryIcon.MESSAGE,
                    label = "Text",
                    action = Action.Sms("088 525 7470"),
                ),
                ContactQuickActionUiModel(
                    icon = ContactEntryIcon.VIDEO_CALL,
                    label = "Video",
                    action = Action.VideoCall("088 525 7470"),
                ),
                ContactQuickActionUiModel(
                    icon = ContactEntryIcon.EMAIL,
                    label = "Email",
                    action = null,
                ),
            ),
            onActionClick = {},
        )
    }
}
