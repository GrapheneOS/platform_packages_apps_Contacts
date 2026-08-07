package com.android.contacts.ui.settings.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.settings.screen.model.SettingsChoice
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val DIALOG_CONTENT_PADDING = 24.dp
private val CHOICE_VERTICAL_PADDING = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SettingsSingleChoiceDialog(
    title: String,
    options: ImmutableList<SettingsChoice<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(Modifier.padding(vertical = DIALOG_CONTENT_PADDING)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AlertDialogDefaults.titleContentColor,
                    modifier = Modifier.padding(
                        start = DIALOG_CONTENT_PADDING,
                        end = DIALOG_CONTENT_PADDING,
                        bottom = 16.dp,
                    ),
                )

                options.forEach { option ->
                    SettingsChoiceRow(
                        choice = option,
                        isSelected = option.value == selected,
                        onSelect = { onSelect(option.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SettingsChoiceRow(
    choice: SettingsChoice<T>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(
                horizontal = DIALOG_CONTENT_PADDING,
                vertical = CHOICE_VERTICAL_PADDING,
            ),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = choice.label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsSingleChoiceDialogPreview() {
    ContactsPreviewTheme {
        SettingsSingleChoiceDialog(
            title = "Sort by",
            options = persistentListOf(
                SettingsChoice(
                    value = "given",
                    label = "First name",
                ),
                SettingsChoice(
                    value = "family",
                    label = "Last name",
                ),
            ),
            selected = "given",
            onSelect = {},
            onDismissRequest = {},
        )
    }
}
