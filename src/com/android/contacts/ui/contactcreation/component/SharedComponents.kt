package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Field row with 4dp vertical padding.
 */
@Composable
internal fun FieldRow(
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * Row with "Add X" at start and optional "Remove X" at end.
 * Used for repeatable field sections (phone, email, address, etc.).
 */
@Composable
internal fun AddRemoveFieldRow(
    addLabel: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    addTestTag: String = "",
    removeLabel: String? = null,
    onRemove: (() -> Unit)? = null,
    removeTestTag: String = "",
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = addLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(onClick = onAdd)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .then(if (addTestTag.isNotEmpty()) Modifier.testTag(addTestTag) else Modifier),
        )
        if (removeLabel != null && onRemove != null) {
            Text(
                text = removeLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .then(
                        if (removeTestTag.isNotEmpty()) {
                            Modifier.testTag(removeTestTag)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

/**
 * Simple add-field text link. For sections without remove capability.
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
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    )
}

/**
 * Text-based remove button in error color.
 * Used for single-instance optional sections (org, nickname, sip, note).
 */
@Composable
internal fun RemoveFieldButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = contentDescription,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    )
}
