package com.android.contacts.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.core.itemClipShape
import com.android.contacts.ui.selection.screen.model.ContactUiModel

private val ITEM_HORIZONTAL_PADDING = 16.dp
private val ITEM_VERTICAL_PADDING = 12.dp
private val CONTACT_AVATAR_SIZE = 36.dp
private val SELECTED_CHECKBOX_ANIMATION_SPEC = spring<Float>(stiffness = Spring.StiffnessHigh / 2)

@Composable
internal fun ContactCell(
    contact: ContactUiModel,
    isSelectable: Boolean,
    showAvatar: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = when {
            contact.isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        shape = itemClipShape(
            isFirst = isFirst,
            isLast = isLast,
            isSelected = contact.isSelected,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .run {
                    when {
                        isSelectable -> toggleable(
                            value = contact.isSelected,
                            enabled = true,
                            role = Role.Checkbox,
                            onValueChange = { onClick() },
                        )
                        else -> clickable(onClick = onClick)
                    }
                }
                .padding(
                    horizontal = ITEM_HORIZONTAL_PADDING,
                    vertical = ITEM_VERTICAL_PADDING,
                ),
        ) {
            if (showAvatar) {
                ContactAvatar(contact)
                Spacer(Modifier.width(16.dp))
            }

            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                contact.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SelectedIcon(isSelected: Boolean) {
    AnimatedVisibility(
        visible = isSelected,
        enter = scaleIn(SELECTED_CHECKBOX_ANIMATION_SPEC),
        exit = scaleOut(SELECTED_CHECKBOX_ANIMATION_SPEC),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(CONTACT_AVATAR_SIZE)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .padding(8.dp),
        )
    }
}

@Composable
private fun ContactAvatar(contact: ContactUiModel) {
    ContactAvatar(
        avatarImage = contact.avatarImage,
        size = CONTACT_AVATAR_SIZE,
        fallbackLabel = contactAvatarLabel(contact.name),
        colorSeed = contactAvatarColorSeed(contact.name),
        fallbackIcon = Icons.Rounded.Person,
        isSelected = contact.isSelected,
    )
}

@PreviewLightDark
@Composable
private fun ContactCellWithoutAvatarPreview() {
    ContactsPreviewColumn {
        ContactCell(
            contact = ContactUiModel(
                key = ContactItemKey.Local(1L),
                name = "John Smith",
                description = "Home · 1 Main Street",
            ),
            isSelectable = false,
            showAvatar = false,
            onClick = {},
            isFirst = true,
            isLast = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactCellSelectableGroupPreview() {
    val contacts = listOf(
        ContactUiModel(
            ContactItemKey.Local(1L),
            "Alice",
            description = "+1 234 456 789",
            isSelected = true,
        ),
        ContactUiModel(ContactItemKey.Local(2L), "Brian Smith"),
        ContactUiModel(ContactItemKey.Local(3L), "Carl", isSelected = true),
        ContactUiModel(ContactItemKey.Local(4L), "David"),
    )
    ContactsPreviewColumn(Modifier.background(MaterialTheme.colorScheme.background)) {
        contacts.forEachIndexed { index, contact ->
            ContactCell(
                contact = contact,
                isSelectable = true,
                showAvatar = true,
                onClick = {},
                isFirst = index == 0,
                isLast = index == contacts.lastIndex,
            )
        }
    }
}
