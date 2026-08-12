package com.android.contacts.ui.vcardexport.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.core.itemClipShape
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_BUTTON_SHARE_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_BUTTON_VCF_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_DIALOG_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.ExportMode
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardAction as Action
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardUiState as State
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun ExportVCardContent(
    uiState: State,
    modifier: Modifier = Modifier,
    onAction: (Action) -> Unit = {},
) {
    if (!uiState.showModeDialog) return

    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(
        onDismissRequest = { onAction(Action.ModeSelected(null)) },
        sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false,
        ),
        sheetGesturesEnabled = false,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag(EXPORT_VCARD_DIALOG_TEST_TAG),
        content = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_export),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 32.dp, bottom = 8.dp),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                ) {
                    ExportOptionsList(uiState, onAction)
                }
            }
        },
    )
}

@Composable
private fun ExportOptionsList(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        uiState.availableModes.forEachIndexed { index, mode ->
            item(key = mode.name) {
                Surface(
                    shape = itemClipShape(
                        isFirst = index == 0,
                        isLast = index == uiState.availableModes.size - 1,
                        shape = MaterialTheme.shapes.medium,
                    ),
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onAction(Action.ModeSelected(mode)) })
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .sizeIn(minHeight = 40.dp)
                            .testTag(
                                when (mode) {
                                    ExportMode.VCARD_FILE -> EXPORT_VCARD_BUTTON_VCF_TEST_TAG
                                    ExportMode.SHARE_ALL -> EXPORT_VCARD_BUTTON_SHARE_TEST_TAG
                                }
                            ),
                    ) {
                        Text(
                            text = stringResource(
                                when (mode) {
                                    ExportMode.VCARD_FILE -> R.string.export_to_vcf_file
                                    ExportMode.SHARE_ALL -> R.string.share_contacts
                                },
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ExportVCardDialogExportingPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ExportVCardContent(
                uiState = State(
                    showModeDialog = true,
                    availableModes = persistentSetOf(ExportMode.VCARD_FILE, ExportMode.SHARE_ALL),
                ),
            )
        }
    }
}
