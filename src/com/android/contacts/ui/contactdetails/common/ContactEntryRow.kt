package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactEntryAction as Action
import com.android.contacts.ui.common.text.asLtrText
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ENHANCED_CALL_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX
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
    onCallingSimClick: () -> Unit,
    onEntryActionClick: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val actions = entryRowActions(
        entry = entry,
        onCopyClick = onCopyClick,
        onOpenActionsMenu = { isActionsMenuExpanded = true },
    )
    val hasTrailingAction = entry.enhancedCallAction != null || entry.alternateAction != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + entry.id)
            .combinedClickable(
                enabled = onClick != null || actions.onLongClick != null,
                onLongClick = actions.onLongClick,
                onLongClickLabel = actions.longClickLabel,
                onClick = onClick ?: actions.onTap ?: {},
            )
            .padding(entryPadding(hasTrailingAction)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = entry.icon
        if (icon != null) {
            when {
                isIconVisible -> EntryLeadingIcon(icon = icon)
                else -> Spacer(modifier = Modifier.width(Tokens.rowIconSize))
            }

            Spacer(modifier = Modifier.width(Tokens.rowIconSpacing))
        }

        EntryText(
            entry = entry,
            modifier = Modifier.weight(1f),
        )

        EntryTrailingActions(
            entry = entry,
            onEntryActionClick = onEntryActionClick,
        )

        if (actions.hasMenu) {
            ContactEntryActionsMenu(
                entry = entry,
                isExpanded = isActionsMenuExpanded,
                onDismissRequest = { isActionsMenuExpanded = false },
                onCopyClick = onCopyClick,
                onSetDefaultClick = onSetDefaultClick,
                onClearDefaultClick = onClearDefaultClick,
                onEditBeforeCallClick = onEntryActionClick,
                onCallingSimClick = onCallingSimClick,
            )
        }
    }
}

private class EntryRowActions(
    val onLongClick: (() -> Unit)?,
    val longClickLabel: String?,
    val onTap: (() -> Unit)?,
    val hasMenu: Boolean,
)

@Composable
private fun entryRowActions(
    entry: ContactEntryUiModel,
    onCopyClick: () -> Unit,
    onOpenActionsMenu: () -> Unit,
): EntryRowActions {
    val hasOtherMenuItems = entry.isDefaultChangeable ||
        entry.isCallingSimChangeable ||
        entry.editBeforeCallAction != null
    val isCopyOnly = entry.copyText != null && !hasOtherMenuItems
    val hasMenu = !isCopyOnly && (entry.copyText != null || hasOtherMenuItems)

    return EntryRowActions(
        onLongClick = when {
            isCopyOnly -> onCopyClick
            hasMenu -> onOpenActionsMenu
            else -> null
        },
        longClickLabel = when {
            isCopyOnly -> stringResource(R.string.copy_text)
            hasMenu -> stringResource(R.string.more_options)
            else -> null
        },
        onTap = onOpenActionsMenu.takeIf { hasMenu },
        hasMenu = hasMenu,
    )
}

private fun entryPadding(hasTrailingAction: Boolean): PaddingValues {
    return PaddingValues(
        start = Tokens.rowHorizontalPadding,
        end = when {
            hasTrailingAction -> Tokens.rowActionEndPadding
            else -> Tokens.rowHorizontalPadding
        },
        top = Tokens.rowVerticalPadding,
        bottom = Tokens.rowVerticalPadding,
    )
}

@Composable
private fun EntryTrailingActions(
    entry: ContactEntryUiModel,
    onEntryActionClick: (Action) -> Unit,
) {
    val enhancedCallAction = entry.enhancedCallAction
    if (enhancedCallAction != null) {
        EntryActionButton(
            action = enhancedCallAction,
            testTag = CONTACT_DETAILS_ENHANCED_CALL_ACTION_TEST_TAG_PREFIX + entry.id,
            onClick = { onEntryActionClick(enhancedCallAction.action) },
        )
    }

    val alternateAction = entry.alternateAction
    if (alternateAction != null) {
        EntryActionButton(
            action = alternateAction,
            testTag = CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX + entry.id,
            onClick = { onEntryActionClick(alternateAction.action) },
        )
    }
}

@Composable
private fun EntryLeadingIcon(icon: ContactEntryIcon) {
    Icon(
        imageVector = icon.imageVector(),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(Tokens.rowIconSize),
    )
}

@Composable
private fun EntryText(
    entry: ContactEntryUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val header = entry.headerText()
        if (header != null) {
            Text(
                text = header,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
        modifier = when {
            isFirst -> Modifier
            else -> Modifier.padding(top = Tokens.rowTextSpacing)
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
            .size(Tokens.rowActionSize)
            .testTag(testTag),
    ) {
        Icon(
            imageVector = action.icon.imageVector(),
            contentDescription = action.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactEntryUiModel.headerText(): String? {
    val header = header ?: return null

    return when {
        isHeaderLtr -> header.asLtrText()
        else -> header
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
            onCallingSimClick = {},
            onEntryActionClick = {},
        )
        ContactEntryRow(
            entry = previewNoteEntry(id = 2L),
            isIconVisible = false,
            onClick = {},
            onCopyClick = {},
            onSetDefaultClick = {},
            onClearDefaultClick = {},
            onCallingSimClick = {},
            onEntryActionClick = {},
        )
    }
}

private fun previewPhoneEntry(
    id: Long,
    number: String = "088 525 7470",
    label: String = "Mobile",
    isSuperPrimary: Boolean = false,
): ContactEntryUiModel {
    val action = Action.Sms(number = number)

    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = isSuperPrimary,
        isDefault = isSuperPrimary,
        isDefaultChangeable = true,
        isCallingSimChangeable = false,
        icon = ContactEntryIcon.CALL,
        header = number,
        isHeaderLtr = true,
        subHeader = null,
        text = label,
        action = Action.Call(number = number),
        alternateAction = ContactEntryActionUiModel(
            action = action,
            icon = ContactEntryIcon.MESSAGE,
            contentDescription = "Text $number",
        ),
        enhancedCallAction = null,
        editBeforeCallAction = Action.EditNumberBeforeCall(number = number),
        copyText = number,
        copyLabel = "Phone",
    )
}

private fun previewNoteEntry(id: Long): ContactEntryUiModel {
    return ContactEntryUiModel(
        id = id,
        isSuperPrimary = false,
        isDefault = false,
        isDefaultChangeable = false,
        isCallingSimChangeable = false,
        icon = null,
        header = "Note",
        isHeaderLtr = false,
        subHeader = null,
        text = "Met at the conference",
        action = null,
        alternateAction = null,
        enhancedCallAction = null,
        editBeforeCallAction = null,
        copyText = "Met at the conference",
        copyLabel = "Note",
    )
}
