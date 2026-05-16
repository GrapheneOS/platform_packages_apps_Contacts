package com.android.contacts.editornew.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R

@Composable
internal fun ContactEditorTopAppBar(
    title: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel_button_content_description),
                )
            }
        },
        actions = {
            Button(
                onClick = onSave,
            ) {
                Text(
                    text = stringResource(R.string.menu_save),
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
        },
    )
}
