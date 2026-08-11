package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DialerSip
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.common.text.asLtrText
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_THIRD_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun ContactEntryRow(
    entry: ContactEntryUiModel,
    isIconVisible: Boolean,
    onClick: (() -> Unit)?,
    onCopyClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    onClearDefaultClick: () -> Unit,
    onAlternateActionClick: (ContactEntryAction) -> Unit,
    onThirdActionClick: (ContactEntryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionsMenuLabel = stringResource(R.string.more_options)
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val hasActionsMenu = entry.copyText != null || entry.isDefaultChangeable
    val hasTrailingAction = entry.thirdAction != null || entry.alternateAction != null
    val openActionsMenu: () -> Unit = { isActionsMenuExpanded = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + entry.id)
            .combinedClickable(
                enabled = onClick != null || hasActionsMenu,
                onLongClick = when {
                    hasActionsMenu -> openActionsMenu
                    else -> null
                },
                onLongClickLabel = when {
                    hasActionsMenu -> actionsMenuLabel
                    else -> null
                },
                onClick = onClick ?: openActionsMenu,
            )
            .padding(
                start = ContactDetailsTokens.rowHorizontalPadding,
                end = when {
                    hasTrailingAction -> ContactDetailsTokens.rowActionEndPadding
                    else -> ContactDetailsTokens.rowHorizontalPadding
                },
                top = ContactDetailsTokens.rowVerticalPadding,
                bottom = ContactDetailsTokens.rowVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = entry.icon
        if (icon != null) {
            EntryLeadingIcon(
                icon = icon,
                isVisible = isIconVisible,
            )

            Spacer(modifier = Modifier.width(ContactDetailsTokens.rowIconSpacing))
        }

        EntryText(
            entry = entry,
            modifier = Modifier.weight(1f),
        )

        val thirdAction = entry.thirdAction
        if (thirdAction != null) {
            EntryActionButton(
                action = thirdAction,
                testTag = CONTACT_DETAILS_THIRD_ACTION_TEST_TAG_PREFIX + entry.id,
                onClick = { onThirdActionClick(thirdAction.action) },
            )
        }

        val alternateAction = entry.alternateAction
        if (alternateAction != null) {
            EntryActionButton(
                action = alternateAction,
                testTag = CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX + entry.id,
                onClick = { onAlternateActionClick(alternateAction.action) },
            )
        }

        ContactEntryActionsMenu(
            entry = entry,
            isExpanded = isActionsMenuExpanded,
            onDismissRequest = { isActionsMenuExpanded = false },
            onCopyClick = {
                isActionsMenuExpanded = false
                onCopyClick()
            },
            onSetDefaultClick = {
                isActionsMenuExpanded = false
                onSetDefaultClick()
            },
            onClearDefaultClick = {
                isActionsMenuExpanded = false
                onClearDefaultClick()
            },
        )
    }
}

@Composable
private fun EntryLeadingIcon(
    icon: ContactEntryIcon,
    isVisible: Boolean,
) {
    Box(modifier = Modifier.size(ContactDetailsTokens.rowIconSize)) {
        if (isVisible) {
            Icon(
                imageVector = icon.imageVector(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EntryText(
    entry: ContactEntryUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val header = entry.header
        if (header != null) {
            Text(
                text = header.asLtrText(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        }

        val subHeader = entry.subHeader
        if (subHeader != null) {
            EntrySecondaryText(
                text = subHeader,
                isFirst = header == null,
            )
        }

        val text = entry.text
        if (text != null) {
            EntrySecondaryText(
                text = text,
                isFirst = header == null && subHeader == null,
            )
        }
    }
}

@Composable
private fun EntrySecondaryText(
    text: String,
    isFirst: Boolean,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = when {
            isFirst -> Modifier
            else -> Modifier.padding(top = ContactDetailsTokens.rowTextSpacing)
        },
    )
}

@Composable
private fun EntryActionButton(
    action: ContactEntryActionUiModel,
    testTag: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(ContactDetailsTokens.rowActionSize)
            .testTag(testTag),
    ) {
        Icon(
            imageVector = action.icon.imageVector(),
            contentDescription = action.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun ContactEntryIcon.imageVector(): ImageVector {
    return when (this) {
        ContactEntryIcon.CALL -> Icons.Rounded.Call
        ContactEntryIcon.MESSAGE -> Icons.AutoMirrored.Rounded.Message
        ContactEntryIcon.VIDEO_CALL -> Icons.Rounded.Videocam
        ContactEntryIcon.CALL_WITH_NOTE -> Icons.AutoMirrored.Rounded.SpeakerNotes
        ContactEntryIcon.EMAIL -> Icons.Rounded.Email
        ContactEntryIcon.PLACE -> Icons.Rounded.Place
        ContactEntryIcon.DIRECTIONS -> Icons.Rounded.Directions
        ContactEntryIcon.SIP_CALL -> Icons.Rounded.DialerSip
    }
}

@PreviewLightDark
@Composable
private fun ContactEntryRowPreview() {
    ContactsPreviewColumn {
        ContactEntryRow(
            entry = previewPhoneEntry(id = 1L),
            isIconVisible = true,
            onClick = {},
            onCopyClick = {},
            onSetDefaultClick = {},
            onClearDefaultClick = {},
            onAlternateActionClick = {},
            onThirdActionClick = {},
        )
        ContactEntryRow(
            entry = previewNoteEntry(id = 2L),
            isIconVisible = false,
            onClick = {},
            onCopyClick = {},
            onSetDefaultClick = {},
            onClearDefaultClick = {},
            onAlternateActionClick = {},
            onThirdActionClick = {},
        )
    }
}

private fun previewPhoneEntry(
    id: Long,
    number: String = "088 525 7470",
    label: String = "Mobile",
    isSuperPrimary: Boolean = false,
): ContactEntryUiModel {
    val action = ContactEntryAction.Sms(number = number)

    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = isSuperPrimary,
        isDefaultChangeable = true,
        icon = ContactEntryIcon.CALL,
        header = number,
        subHeader = null,
        text = label,
        action = ContactEntryAction.Call(number = number),
        alternateAction = ContactEntryActionUiModel(
            action = action,
            icon = ContactEntryIcon.MESSAGE,
            contentDescription = "Text $number",
        ),
        thirdAction = null,
        copyText = number,
        copyLabel = "Phone",
    )
}

private fun previewNoteEntry(id: Long): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = false,
        isDefaultChangeable = false,
        icon = null,
        header = "Note",
        subHeader = null,
        text = "Met at the conference",
        action = null,
        alternateAction = null,
        thirdAction = null,
        copyText = "Met at the conference",
        copyLabel = "Note",
    )
}
