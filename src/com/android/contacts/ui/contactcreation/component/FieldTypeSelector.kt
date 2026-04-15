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

/**
 * Generic type selector with FilterChip + DropdownMenu.
 *
 * [labels] is a pre-computed list of display strings matching [types] by index.
 * Pre-computing avoids passing @Composable lambdas into DropdownMenu's separate
 * Popup composition, which can null-out captured generic parameters at runtime.
 */
@Composable
internal fun <T : Any> FieldTypeSelector(
    currentLabel: String,
    types: List<T>,
    labels: List<String>,
    onTypeSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text(currentLabel) },
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
            types.forEachIndexed { index, type ->
                val label = labels[index]
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
        val types = listOf("Mobile", "Home", "Work", "Other")
        FieldTypeSelector(
            currentLabel = "Mobile",
            types = types,
            labels = types,
            onTypeSelected = {},
        )
    }
}
