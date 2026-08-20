package com.android.contacts.ui.simimport.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
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
import com.android.contacts.ui.common.model.SelectableItem
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.simimport.screen.model.SimContactUiModel

@Composable
internal fun SimContactCell(
    contact: SimContactUiModel,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    SimContactInnerCell(
        contact = contact,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        isSelectable = false,
        isSelected = false,
        onSelectedChange = null,
    )
}

@Composable
internal fun SimContactSelectableCell(
    item: SelectableItem<SimContactUiModel>,
    isFirst: Boolean,
    isLast: Boolean,
    onSelectedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
) {
    SimContactInnerCell(
        contact = item.item,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        isSelectable = true,
        isSelected = item.isSelected,
        onSelectedChange = onSelectedChange,
    )
}

@Composable
private fun SimContactInnerCell(
    contact: SimContactUiModel,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    onSelectedChange: ((Boolean) -> Unit)? = null,
) {
    Surface(
        color = when {
            isSelected || !isSelectable -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        shape = itemClipShape(
            isFirst = isFirst,
            isLast = isLast,
            isSelected = isSelected,
        ),
        modifier = modifier,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .run {
                    if (!isSelectable || onSelectedChange == null) return@run this
                    toggleable(
                        value = isSelected,
                        enabled = true,
                        role = Role.Checkbox,
                        onValueChange = onSelectedChange,
                    )
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
    val defaultCornerSize = CornerSize(2.dp)
    return RoundedCornerShape(
        topStart = if (isFirst || isSelected) cornerSize else defaultCornerSize,
        topEnd = if (isFirst || isSelected) cornerSize else defaultCornerSize,
        bottomStart = if (isLast || isSelected) cornerSize else defaultCornerSize,
        bottomEnd = if (isLast || isSelected) cornerSize else defaultCornerSize,
    )
}

@PreviewLightDark
@Composable
private fun SimContactCellPreview() {
    ContactsPreviewColumn {
        SimContactCell(
            contact = SimContactUiModel(1, "Anna Smith"),
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
                item = SimContactUiModel(1, "Anna Smith"),
                isSelected = false,
            ),
            isFirst = false,
            isLast = false,
            onSelectedChange = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SimContactCellSelectedPreview() {
    ContactsPreviewColumn {
        SimContactSelectableCell(
            SelectableItem(
                item = SimContactUiModel(1, "Anna Smith"),
                isSelected = true,
            ),
            isFirst = false,
            isLast = false,
            onSelectedChange = {},
        )
    }
}
