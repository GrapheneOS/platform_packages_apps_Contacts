package com.android.contacts.ui.settings.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp,
            ),
    )
}

@PreviewLightDark
@Composable
private fun SettingsSectionHeaderPreview() {
    ContactsPreviewColumn {
        SettingsSectionHeader(title = "Display options")
    }
}
