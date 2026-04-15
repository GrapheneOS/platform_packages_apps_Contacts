package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Box
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
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.core.AppTheme

@Composable
internal fun <T> FieldTypeSelector(
    currentType: T,
    types: List<T>,
    typeLabel: @Composable (T) -> String,
    onTypeSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(typeLabel(currentType)) },
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
            types.forEach { type ->
                val label = typeLabel(type)
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onTypeSelected(type)
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
        FieldTypeSelector(
            currentType = "Mobile",
            types = listOf("Mobile", "Home", "Work", "Other"),
            typeLabel = { it },
            onTypeSelected = {},
        )
    }
}
