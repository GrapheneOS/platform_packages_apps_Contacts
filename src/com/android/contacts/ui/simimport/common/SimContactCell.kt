package com.android.contacts.ui.simimport.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.model.SimContact
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun SimContactCell(
    contact: SimContact,
    isFirst: Boolean,
    isLast: Boolean,
) {
    SimContactInnerCell(
        contact = contact,
        isSelectable = false,
        isSelected = false,
        isFirst = isFirst,
        isLast = isLast,
        onClick = null,
    )
}

@Composable
internal fun SimContactSelectableCell(
    item: SelectableItem<SimContact>,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: (() -> Unit),
) {
    SimContactInnerCell(
        contact = item.item,
        isSelectable = true,
        isSelected = item.isSelected,
        isFirst = isFirst,
        isLast = isLast,
        onClick = onClick,
    )
}

@Composable
private fun SimContactInnerCell(
    contact: SimContact,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        color = if (isSelected || !isSelectable) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = itemClipShape(
            isFirst = isFirst,
            isLast = isLast,
            isSelected = isSelected,
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .run {
                    if (isSelectable && onClick != null) {
                        selectable(
                            selected = isSelected,
                            enabled = true,
                            role = Role.Checkbox,
                            onClick = onClick,
                        )
                    } else {
                        this
                    }
                }
                .padding(vertical = 12.dp, horizontal = 16.dp),
        ) {
            Text(
                text = contact.label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
            )
            if (isSelectable) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                )
            }
        }
    }
}

@Composable
private fun itemClipShape(
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean,
): Shape {
    // All corners are the same in our shapes
    val cornerSize = MaterialTheme.shapes.extraSmall.topStart
    return RoundedCornerShape(
        topStart = if (isFirst || isSelected) cornerSize else CornerSize(0.dp),
        topEnd = if (isFirst || isSelected) cornerSize else CornerSize(0.dp),
        bottomStart = if (isLast || isSelected) cornerSize else CornerSize(0.dp),
        bottomEnd = if (isLast || isSelected) cornerSize else CornerSize(0.dp),
    )
}

private val SimContact.label
    get() =
        if (hasName()) {
            name
        } else if (hasPhone()) {
            phone
        } else if (hasEmails()) {
            emails[0]
        } else {
            // This isn't really possible because we skip empty SIM contacts during loading
            ""
        }

@PreviewLightDark
@Composable
private fun SimContactCellPreview() {
    ContactsPreviewColumn {
        SimContactCell(
            contact = SimContact(1, "Anna Smith", null),
            isFirst = false,
            isLast = false,
        )
    }
}

@PreviewLightDark
@Composable
private fun SimContactCellDeselectedPreview() {
    ContactsPreviewColumn {
        SimContactSelectableCell(
            SelectableItem(
                item = SimContact(1, "Anna Smith", null),
                isSelected = false,
            ),
            isFirst = false,
            isLast = false,
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimContactCellSelectedPreview() {
    ContactsPreviewColumn {
        SimContactSelectableCell(
            SelectableItem(
                item = SimContact(1, "Anna Smith", null),
                isSelected = true,
            ),
            isFirst = false,
            isLast = false,
            onClick = {},
        )
    }
}
