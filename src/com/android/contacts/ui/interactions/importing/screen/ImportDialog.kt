package com.android.contacts.ui.interactions.importing.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.common.util.expandStringTemplate
import com.android.contacts.ui.common.util.messageFormatResource
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.core.itemClipShape
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_EMPTY_MESSAGE_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_PROGRESS_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_SIM_CARD_BUTTON_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_VCARD_BUTTON_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportUiState as State
import com.android.contacts.ui.interactions.importing.screen.model.SimCardOption
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun ImportDialog(
    effectHandler: ImportEffectHandler,
    modifier: Modifier = Modifier,
    screenModel: ImportScreenModel = viewModel<ImportViewModel>(),
    accountChosen: AccountModel? = null,
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()
    val effectHandler by rememberUpdatedState(effectHandler)

    LaunchedEffect(screenModel) {
        screenModel.effects.collect(effectHandler::handle)
    }

    LaunchedEffect(screenModel, accountChosen) {
        accountChosen?.let { screenModel.onAction(Action.AccountChosen(accountChosen)) }
    }

    ImportDialogContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun ImportDialogContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(
        onDismissRequest = { onAction(Action.Dismiss) },
        sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = false,
        ),
        sheetGesturesEnabled = false,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
        content = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_import),
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
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                                    .size(32.dp)
                                    .testTag(IMPORT_PROGRESS_TEST_TAG),
                            )
                        }

                        uiState.noOptionsAvailable -> {
                            Text(
                                text = stringResource(R.string.nothing_to_import_message),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                                    .testTag(IMPORT_EMPTY_MESSAGE_TEST_TAG),
                            )
                        }

                        else -> {
                            ImportOptionsList(uiState, onAction)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ImportOptionsList(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (uiState.isVCardImportAvailable == true) {
            item(key = "vcard") {
                VCardCell(uiState, onAction)
            }
        }
        itemsIndexed(
            items = uiState.simCardOptions.orEmpty(),
            key = { _, option -> "sim_${option.subscriptionId}" },
        ) { index, option ->
            SimCardCell(
                uiState = uiState,
                onAction = onAction,
                index = index,
                option = option,
            )
        }
    }
}

@Composable
private fun VCardCell(
    uiState: State,
    onAction: (Action) -> Unit,
) {
    OptionCell(
        isFirst = true,
        isLast = uiState.simCardOptions.isNullOrEmpty(),
        onClick = { onAction(Action.VCardClick) },
        modifier = Modifier.testTag(IMPORT_VCARD_BUTTON_TEST_TAG),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_upload_file_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = stringResource(R.string.import_from_vcf_file),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SimCardCell(
    uiState: State,
    onAction: (Action) -> Unit,
    index: Int,
    option: SimCardOption,
) {
    OptionCell(
        isFirst = index == 0 && uiState.isVCardImportAvailable != true,
        isLast = index == uiState.simCardOptions?.lastIndex,
        onClick = { onAction(Action.SimOptionClick(option)) },
        modifier = Modifier.testTag(IMPORT_SIM_CARD_BUTTON_TEST_TAG),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.quantum_ic_sim_card_vd_theme_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column {
                Text(
                    text = stringResource(
                        R.string.import_from_sim_summary_fmt,
                        option.name ?: (index + 1),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                simCardOptionDescription(option)?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionCell(
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = itemClipShape(
            isFirst = isFirst,
            isLast = isLast,
            shape = MaterialTheme.shapes.medium,
        ),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .sizeIn(minHeight = 40.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun simCardOptionDescription(option: SimCardOption): String? {
    return when {
        option.contactsCount != null && !option.phone.isNullOrBlank() -> {
            expandStringTemplate(
                messageFormatResource(
                    R.string.import_from_sim_secondary_template,
                    persistentMapOf("count" to option.contactsCount),
                ),
                option.phone,
            )
        }

        option.contactsCount != null -> {
            messageFormatResource(
                R.string.import_from_sim_secondary_contact_count_fmt,
                persistentMapOf("count" to option.contactsCount),
            )
        }

        !option.phone.isNullOrBlank() -> option.phone

        else -> null
    }
}

@PreviewLightDark
@Composable
private fun ImportDialogProgressPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportDialogContent(
                uiState = State(
                    isVCardImportAvailable = null,
                    simCardOptions = null,
                ),
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ImportDialogEmptyPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportDialogContent(
                uiState = State(
                    isVCardImportAvailable = false,
                    simCardOptions = persistentListOf(),
                ),
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ImportDialogPreview() {
    ContactsPreviewTheme {
        Box(Modifier.fillMaxSize()) {
            ImportDialogContent(
                uiState = State(
                    isVCardImportAvailable = true,
                    simCardOptions = persistentListOf(
                        SimCardOption(
                            subscriptionId = 1,
                            name = "John Smith",
                            contactsCount = 10,
                            phone = "123 456 789",
                        ),
                        SimCardOption(
                            subscriptionId = 2,
                            name = null,
                            contactsCount = null,
                            phone = null,
                        ),
                    ),
                ),
                onAction = {},
            )
        }
    }
}
