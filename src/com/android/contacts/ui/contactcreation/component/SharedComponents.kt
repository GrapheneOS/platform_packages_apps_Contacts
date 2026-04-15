@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private val IconColumnWidth = 40.dp
private val IconSize = 24.dp
private val SectionHeaderStartPadding = 56.dp
private val AddFieldButtonStartPadding = 56.dp

/**
 * Section header: titleSmall, primary color, 56dp start padding.
 * Top=24dp, bottom=8dp per M3 form spec.
 */
@Composable
internal fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = SectionHeaderStartPadding, top = 24.dp, bottom = 8.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    )
}

/**
 * Consistent field row with 40dp icon column.
 * [icon] is shown only for the first field in a section; subsequent fields pass null.
 */
@Composable
internal fun FieldRow(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(IconColumnWidth),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(IconSize),
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * Add-field text link: 56dp start padding, primary color, plain text.
 * Matches Google Contacts "Add phone" / "Add email" style.
 */
@Composable
internal fun AddFieldButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(start = AddFieldButtonStartPadding)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    )
}

/**
 * Red outlined circle remove button with minus icon.
 * Matches Google Contacts style. 48dp minimum touch target.
 */
@Composable
internal fun RemoveFieldButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    OutlinedIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = modifier,
    ) {
        Icon(
            Icons.Outlined.Remove,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
    }
}
