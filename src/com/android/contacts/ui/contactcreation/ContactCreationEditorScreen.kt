@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.contacts.ui.contactcreation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.model.account.AccountWithDataSet
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
import com.android.contacts.ui.contactcreation.component.SipField
import com.android.contacts.ui.contactcreation.component.WebsiteSectionContent
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import kotlinx.coroutines.CancellationException

@Composable
internal fun ContactCreationEditorScreen(
    uiState: ContactCreationUiState,
    accounts: List<AccountWithDataSet>,
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
            accounts = accounts,
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
    accounts: List<AccountWithDataSet>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAccountSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .imePadding(),
    ) {
        PhotoAndAccountHeader(uiState = uiState, onAction = onAction)
        FieldSections(uiState = uiState, onAction = onAction)
        AccountFooterBar(
            accountName = uiState.accountName,
            showPicker = accounts.size > 1,
            onTap = { showAccountSheet = true },
        )
        Spacer(modifier = Modifier.height(48.dp))
    }

    if (showAccountSheet) {
        AccountBottomSheet(
            accounts = accounts,
            selectedAccount = uiState.selectedAccount,
            onAction = onAction,
            onDismiss = { showAccountSheet = false },
        )
    }
}

@Composable
private fun PhotoAndAccountHeader(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
) {
    PhotoSectionContent(photoUri = uiState.photoUri, onAction = onAction)
    Spacer(modifier = Modifier.height(16.dp))
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun FieldSections(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
) {
    var showOtherSheet by remember { mutableStateOf(false) }

    // --- Always-visible sections ---

    NameSectionContent(nameState = uiState.nameState, onAction = onAction)
    Spacer(modifier = Modifier.height(8.dp))

    PhoneSectionContent(phones = uiState.phoneNumbers, onAction = onAction)
    Spacer(modifier = Modifier.height(16.dp))

    EmailSectionContent(emails = uiState.emails, onAction = onAction)
    Spacer(modifier = Modifier.height(16.dp))

    // --- Conditionally-visible sections ---

    // Address
    AnimatedVisibility(
        visible = uiState.addresses.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            AddressSectionContent(addresses = uiState.addresses, onAction = onAction)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Organization
    AnimatedVisibility(
        visible = uiState.showOrganization || uiState.organization.hasData(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            OrganizationSectionContent(
                organization = uiState.organization,
                onAction = onAction,
            )
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.HideOrganization) },
                contentDescription = "Remove organization",
                modifier = Modifier.testTag(TestTags.ORG_REMOVE),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Nickname
    AnimatedVisibility(
        visible = uiState.showNickname || uiState.nickname.isNotBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            NicknameField(nickname = uiState.nickname, onAction = onAction)
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
        Column {
            SipField(sipAddress = uiState.sipAddress, onAction = onAction)
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
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
            RelationSectionContent(relations = uiState.relations, onAction = onAction)
        }
    }

    // Note
    AnimatedVisibility(
        visible = uiState.showNote || uiState.note.isNotBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            FieldRow {
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
            RemoveFieldButton(
                onClick = { onAction(ContactCreationAction.HideNote) },
                contentDescription = "Remove note",
                modifier = Modifier.testTag(TestTags.NOTE_REMOVE),
            )
        }
    }

    // --- Chip grid (no outer AnimatedVisibility — inner per-chip animations + animateContentSize handle it) ---
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

    Spacer(modifier = Modifier.height(16.dp))

    // Groups
    if (uiState.availableGroups.isNotEmpty()) {
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

@Composable
private fun AccountFooterBar(
    accountName: String?,
    showPicker: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showPicker) Modifier.clickable(onClick = onTap) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.ACCOUNT_FOOTER),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = accountName ?: "Device only",
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "account_crossfade",
        ) { name ->
            Text(
                text = "Saving to $name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showPicker) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccountBottomSheet(
    accounts: List<AccountWithDataSet>,
    selectedAccount: AccountWithDataSet?,
    onAction: (ContactCreationAction) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag(TestTags.ACCOUNT_SHEET),
    ) {
        accounts.forEachIndexed { index, account ->
            val isSelected = account == selectedAccount
            ListItem(
                headlineContent = { Text(account.name ?: "Device") },
                supportingContent = {
                    val typeLabel = account.type ?: "Device"
                    Text(typeLabel)
                },
                trailingContent = {
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                modifier = Modifier
                    .clickable {
                        onAction(ContactCreationAction.SelectAccount(account))
                        onDismiss()
                    }
                    .semantics {
                        role = Role.RadioButton
                        selected = isSelected
                    }
                    .testTag(TestTags.accountSheetItem(index)),
            )
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}
