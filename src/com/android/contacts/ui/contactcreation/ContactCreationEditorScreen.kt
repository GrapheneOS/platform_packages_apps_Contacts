@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.contacts.ui.contactcreation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.component.AccountChip
import com.android.contacts.ui.contactcreation.component.AddMoreInfoSection
import com.android.contacts.ui.contactcreation.component.AddressSectionContent
import com.android.contacts.ui.contactcreation.component.EmailSectionContent
import com.android.contacts.ui.contactcreation.component.EventSectionContent
import com.android.contacts.ui.contactcreation.component.FieldRow
import com.android.contacts.ui.contactcreation.component.GroupSectionContent
import com.android.contacts.ui.contactcreation.component.ImSectionContent
import com.android.contacts.ui.contactcreation.component.NameSectionContent
import com.android.contacts.ui.contactcreation.component.NicknameField
import com.android.contacts.ui.contactcreation.component.OrganizationSectionContent
import com.android.contacts.ui.contactcreation.component.OtherFieldsBottomSheet
import com.android.contacts.ui.contactcreation.component.PhoneSectionContent
import com.android.contacts.ui.contactcreation.component.PhotoSectionContent
import com.android.contacts.ui.contactcreation.component.RelationSectionContent
import com.android.contacts.ui.contactcreation.component.RemoveFieldButton
import com.android.contacts.ui.contactcreation.component.SectionHeader
import com.android.contacts.ui.contactcreation.component.SipField
import com.android.contacts.ui.contactcreation.component.WebsiteSectionContent
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

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun FieldSections(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
) {
    var showOtherSheet by remember { mutableStateOf(false) }

    // --- Always-visible sections ---

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

    // --- Conditionally-visible sections ---

    // Address
    AnimatedVisibility(
        visible = uiState.addresses.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            SectionHeader(
                title = stringResource(R.string.contact_creation_section_address),
                testTag = TestTags.SECTION_HEADER_ADDRESS,
            )
            AddressSectionContent(addresses = uiState.addresses, onAction = onAction)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Organization
    AnimatedVisibility(
        visible = uiState.showOrganization || uiState.organization.hasData(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            SectionHeader(
                title = stringResource(R.string.contact_creation_section_organization),
                testTag = TestTags.SECTION_HEADER_ORGANIZATION,
            )
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    OrganizationSectionContent(
                        organization = uiState.organization,
                        onAction = onAction,
                    )
                }
                RemoveFieldButton(
                    onClick = { onAction(ContactCreationAction.HideOrganization) },
                    contentDescription = "Remove organization",
                    modifier = Modifier.testTag(TestTags.ORG_REMOVE),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Nickname
    AnimatedVisibility(
        visible = uiState.showNickname || uiState.nickname.isNotBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                NicknameField(nickname = uiState.nickname, onAction = onAction)
            }
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.HideNickname) },
                contentDescription = "Remove nickname",
                modifier = Modifier.testTag(TestTags.NICKNAME_REMOVE),
            )
        }
    }

    // SIP
    AnimatedVisibility(
        visible = uiState.showSipField &&
            (uiState.showSipAddress || uiState.sipAddress.isNotBlank()),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                SipField(sipAddress = uiState.sipAddress, onAction = onAction)
            }
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.HideSipAddress) },
                contentDescription = "Remove SIP address",
                modifier = Modifier.testTag(TestTags.SIP_REMOVE),
            )
        }
    }

    // IM
    AnimatedVisibility(
        visible = uiState.imAccounts.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Instant messaging")
            ImSectionContent(imAccounts = uiState.imAccounts, onAction = onAction)
        }
    }

    // Website
    AnimatedVisibility(
        visible = uiState.websites.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Websites")
            WebsiteSectionContent(websites = uiState.websites, onAction = onAction)
        }
    }

    // Events
    AnimatedVisibility(
        visible = uiState.events.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Events")
            EventSectionContent(events = uiState.events, onAction = onAction)
        }
    }

    // Relations
    AnimatedVisibility(
        visible = uiState.relations.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Relations")
            RelationSectionContent(relations = uiState.relations, onAction = onAction)
        }
    }

    // Note
    AnimatedVisibility(
        visible = uiState.showNote || uiState.note.isNotBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                FieldRow(icon = Icons.AutoMirrored.Filled.Notes) {
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { onAction(ContactCreationAction.UpdateNote(it)) },
                        label = { Text(stringResource(R.string.contact_creation_note)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.NOTE_FIELD),
                        singleLine = false,
                        maxLines = 4,
                    )
                }
            }
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.HideNote) },
                contentDescription = "Remove note",
                modifier = Modifier.testTag(TestTags.NOTE_REMOVE),
            )
        }
    }

    // --- Chip grid ---
    AnimatedVisibility(
        visible = uiState.hasAnyChip,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        AddMoreInfoSection(
            showAddressChip = uiState.showAddressChip,
            showOrgChip = uiState.showOrgChip,
            showNoteChip = uiState.showNoteChip,
            showGroupsChip = uiState.showGroupsChip,
            showOtherChip = uiState.showOtherChip,
            onAddAddress = { onAction(ContactCreationAction.AddAddress) },
            onShowOrganization = { onAction(ContactCreationAction.ShowOrganization) },
            onShowNote = { onAction(ContactCreationAction.ShowNote) },
            onShowGroups = {
                // Add first group toggle to show section; actual selection in GroupSectionContent
                // For now just scroll to groups. We show groups section when groups is non-empty
                // or user taps this chip — handled via availableGroups presence check below.
            },
            onShowOtherSheet = { showOtherSheet = true },
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Groups
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

    // Bottom sheet
    if (showOtherSheet) {
        OtherFieldsBottomSheet(
            showEvents = uiState.events.isEmpty(),
            showRelations = uiState.relations.isEmpty(),
            showIm = uiState.imAccounts.isEmpty(),
            showWebsites = uiState.websites.isEmpty(),
            showSip = !uiState.showSipAddress && uiState.sipAddress.isBlank() &&
                uiState.showSipField,
            showNickname = !uiState.showNickname && uiState.nickname.isBlank(),
            onAction = onAction,
            onDismiss = { showOtherSheet = false },
        )
    }
}
