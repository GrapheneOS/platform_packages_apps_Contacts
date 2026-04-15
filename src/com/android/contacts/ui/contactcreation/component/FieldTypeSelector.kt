package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.core.AppTheme

/**
 * Generic type selector with FilterChip + DropdownMenu.
 *
 * Dispatches by index via [onIndexSelected] to avoid sealed-class instances
 * becoming null inside DropdownMenu's separate Popup composition tree.
 */
@Composable
internal fun FieldTypeSelector(
    currentLabel: String,
    labels: List<String>,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Text(
                    currentLabel,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            labels.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onIndexSelected(index)
                    },
                    modifier = Modifier.testTag(TestTags.fieldTypeOption(label)),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FieldTypeSelectorPreview() {
    AppTheme {
        val labels = listOf("Mobile", "Home", "Work", "Other")
        FieldTypeSelector(
            currentLabel = "Mobile",
            labels = labels,
            onIndexSelected = {},
        )
    }
}
