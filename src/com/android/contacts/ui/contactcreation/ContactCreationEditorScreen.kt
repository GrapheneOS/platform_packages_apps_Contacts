@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.contactcreation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import com.android.contacts.ui.contactcreation.component.accountChipItem
import com.android.contacts.ui.contactcreation.component.addressSection
import com.android.contacts.ui.contactcreation.component.emailSection
import com.android.contacts.ui.contactcreation.component.groupSection
import com.android.contacts.ui.contactcreation.component.moreFieldsSection
import com.android.contacts.ui.contactcreation.component.nameSection
import com.android.contacts.ui.contactcreation.component.organizationSection
import com.android.contacts.ui.contactcreation.component.phoneSection
import com.android.contacts.ui.contactcreation.component.photoSection
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import kotlinx.coroutines.CancellationException

@Composable
internal fun ContactCreationEditorScreen(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    PredictiveBackHandler(enabled = true) { flow ->
        try {
            flow.collect { /* consume progress events */ }
            onAction(ContactCreationAction.NavigateBack)
        } catch (_: CancellationException) {
            // Back gesture cancelled, do nothing
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Create contact",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(ContactCreationAction.NavigateBack) },
                        modifier = Modifier.testTag(TestTags.BACK_BUTTON),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(ContactCreationAction.Save) },
                        modifier = Modifier.testTag(TestTags.SAVE_BUTTON),
                        enabled = !uiState.isSaving,
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        ContactCreationFieldsList(
            uiState = uiState,
            onAction = onAction,
            contentPadding = contentPadding,
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
        title = { Text("Discard changes?") },
        text = {
            Text(
                "You have unsaved changes that will be lost.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(ContactCreationAction.ConfirmDiscard) },
                modifier = Modifier.testTag(TestTags.DISCARD_DIALOG_CONFIRM),
            ) {
                Text("Discard")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(ContactCreationAction.DismissDiscardDialog) },
                modifier = Modifier.testTag(TestTags.DISCARD_DIALOG_DISMISS),
            ) {
                Text("Keep editing")
            }
        },
        modifier = Modifier.testTag(TestTags.DISCARD_DIALOG),
    )
}

@Composable
private fun ContactCreationFieldsList(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        photoSection(photoUri = uiState.photoUri, onAction = onAction)
        accountChipItem(accountName = uiState.accountName, onAction = onAction)
        nameSection(nameState = uiState.nameState, onAction = onAction)
        phoneSection(phones = uiState.phoneNumbers, onAction = onAction)
        emailSection(emails = uiState.emails, onAction = onAction)
        addressSection(addresses = uiState.addresses, onAction = onAction)
        organizationSection(organization = uiState.organization, onAction = onAction)
        moreFieldsSection(
            isExpanded = uiState.isMoreFieldsExpanded,
            events = uiState.events,
            relations = uiState.relations,
            imAccounts = uiState.imAccounts,
            websites = uiState.websites,
            note = uiState.note,
            nickname = uiState.nickname,
            sipAddress = uiState.sipAddress,
            showSipField = uiState.showSipField,
            onAction = onAction,
        )
        groupSection(
            availableGroups = uiState.availableGroups,
            selectedGroups = uiState.groups,
            onAction = onAction,
        )
    }
}
