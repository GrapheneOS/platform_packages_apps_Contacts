@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.contacts.ui.contactcreation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.component.AccountChip
import com.android.contacts.ui.contactcreation.component.AddressSectionContent
import com.android.contacts.ui.contactcreation.component.EmailSectionContent
import com.android.contacts.ui.contactcreation.component.GroupSectionContent
import com.android.contacts.ui.contactcreation.component.MoreFieldsSectionContent
import com.android.contacts.ui.contactcreation.component.MoreFieldsState
import com.android.contacts.ui.contactcreation.component.NameSectionContent
import com.android.contacts.ui.contactcreation.component.PhoneSectionContent
import com.android.contacts.ui.contactcreation.component.PhotoSectionContent
import com.android.contacts.ui.contactcreation.component.SectionHeader
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import kotlinx.coroutines.CancellationException

@Composable
internal fun ContactCreationEditorScreen(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PredictiveBackHandler(enabled = true) { flow ->
        try {
            flow.collect { /* consume progress events */ }
            onAction(ContactCreationAction.NavigateBack)
        } catch (_: CancellationException) {
            // Back gesture cancelled, do nothing
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.contact_editor_title_new_contact),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ContactCreationAction.NavigateBack) },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.testTag(TestTags.CLOSE_BUTTON),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(
                                R.string.contact_creation_close,
                            ),
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { onAction(ContactCreationAction.Save) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.testTag(TestTags.SAVE_TEXT_BUTTON),
                        enabled = !uiState.isSaving,
                    ) {
                        Text(stringResource(R.string.contact_creation_save))
                    }
                },
            )
        },
    ) { contentPadding ->
        ContactCreationFieldsColumn(
            uiState = uiState,
            onAction = onAction,
            modifier = Modifier.padding(contentPadding),
        )
    }

    if (uiState.showDiscardDialog) {
        DiscardChangesDialog(onAction = onAction)
    }
}

@Composable
private fun DiscardChangesDialog(onAction: (ContactCreationAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(ContactCreationAction.DismissDiscardDialog) },
        title = { Text(stringResource(R.string.cancel_confirmation_dialog_message)) },
        text = {
            Text(
                stringResource(R.string.contact_creation_discard_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(ContactCreationAction.ConfirmDiscard) },
                modifier = Modifier.testTag(TestTags.DISCARD_DIALOG_CONFIRM),
            ) {
                Text(stringResource(R.string.cancel_confirmation_dialog_cancel_editing_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(ContactCreationAction.DismissDiscardDialog) },
                modifier = Modifier.testTag(TestTags.DISCARD_DIALOG_DISMISS),
            ) {
                Text(stringResource(R.string.contact_creation_keep_editing))
            }
        },
        modifier = Modifier.testTag(TestTags.DISCARD_DIALOG),
    )
}

@Composable
private fun ContactCreationFieldsColumn(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
    ) {
        PhotoAndAccountHeader(uiState = uiState, onAction = onAction)
        FieldSections(uiState = uiState, onAction = onAction)
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun PhotoAndAccountHeader(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
) {
    PhotoSectionContent(photoUri = uiState.photoUri, onAction = onAction)
    Spacer(modifier = Modifier.height(16.dp))
    AccountChip(
        accountName = uiState.accountName,
        onClick = { onAction(ContactCreationAction.RequestAccountPicker) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun FieldSections(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
) {
    SectionHeader(
        title = stringResource(R.string.contact_creation_section_name),
        testTag = TestTags.SECTION_HEADER_NAME,
    )
    NameSectionContent(nameState = uiState.nameState, onAction = onAction)
    Spacer(modifier = Modifier.height(24.dp))

    SectionHeader(
        title = stringResource(R.string.contact_creation_section_phone),
        testTag = TestTags.SECTION_HEADER_PHONE,
    )
    PhoneSectionContent(phones = uiState.phoneNumbers, onAction = onAction)
    Spacer(modifier = Modifier.height(24.dp))

    SectionHeader(
        title = stringResource(R.string.contact_creation_section_email),
        testTag = TestTags.SECTION_HEADER_EMAIL,
    )
    EmailSectionContent(emails = uiState.emails, onAction = onAction)
    Spacer(modifier = Modifier.height(24.dp))

    if (uiState.addresses.isNotEmpty()) {
        SectionHeader(
            title = stringResource(R.string.contact_creation_section_address),
            testTag = TestTags.SECTION_HEADER_ADDRESS,
        )
        AddressSectionContent(addresses = uiState.addresses, onAction = onAction)
        Spacer(modifier = Modifier.height(24.dp))
    }

    MoreFieldsSectionContent(
        state = MoreFieldsState(
            isExpanded = uiState.isMoreFieldsExpanded,
            organization = uiState.organization,
            events = uiState.events,
            relations = uiState.relations,
            imAccounts = uiState.imAccounts,
            websites = uiState.websites,
            note = uiState.note,
            nickname = uiState.nickname,
            sipAddress = uiState.sipAddress,
            showSipField = uiState.showSipField,
        ),
        onAction = onAction,
    )
    Spacer(modifier = Modifier.height(24.dp))

    if (uiState.availableGroups.isNotEmpty()) {
        SectionHeader(
            title = stringResource(R.string.contact_creation_section_groups),
            testTag = TestTags.SECTION_HEADER_GROUPS,
        )
        GroupSectionContent(
            availableGroups = uiState.availableGroups,
            selectedGroups = uiState.groups,
            onAction = onAction,
        )
    }
}
