package com.android.contacts.ui.settings.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_arrow_content_description),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun SettingsTopAppBarPreview() {
    ContactsPreviewTheme {
        SettingsTopAppBar(
            title = "Settings",
            onNavigateBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsTopAppBarLongTitlePreview() {
    ContactsPreviewTheme {
        SettingsTopAppBar(
            title = "Contacts display and account settings",
            onNavigateBack = {},
        )
    }
}
