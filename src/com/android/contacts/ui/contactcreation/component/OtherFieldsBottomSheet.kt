@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

@Composable
internal fun OtherFieldsBottomSheet(
    showEvents: Boolean,
    showRelations: Boolean,
    showIm: Boolean,
    showWebsites: Boolean,
    showSip: Boolean,
    showNickname: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag(TestTags.OTHER_FIELDS_SHEET),
    ) {
        if (showEvents) {
            SheetItem(
                label = "Event",
                icon = Icons.Filled.DateRange,
                section = "event",
                onClick = {
                    onAction(ContactCreationAction.AddEvent)
                    onDismiss()
                },
            )
        }
        if (showRelations) {
            SheetItem(
                label = "Relation",
                icon = Icons.Filled.People,
                section = "relation",
                onClick = {
                    onAction(ContactCreationAction.AddRelation)
                    onDismiss()
                },
            )
        }
        if (showIm) {
            SheetItem(
                label = "Instant messaging",
                icon = Icons.AutoMirrored.Filled.Message,
                section = "im",
                onClick = {
                    onAction(ContactCreationAction.AddIm)
                    onDismiss()
                },
            )
        }
        if (showWebsites) {
            SheetItem(
                label = "Website",
                icon = Icons.Filled.Public,
                section = "website",
                onClick = {
                    onAction(ContactCreationAction.AddWebsite)
                    onDismiss()
                },
            )
        }
        if (showSip) {
            SheetItem(
                label = "SIP address",
                icon = Icons.Filled.Phone,
                section = "sip",
                onClick = {
                    onAction(ContactCreationAction.ShowSipAddress)
                    onDismiss()
                },
            )
        }
        if (showNickname) {
            SheetItem(
                label = "Nickname",
                icon = Icons.Filled.Person,
                section = "nickname",
                onClick = {
                    onAction(ContactCreationAction.ShowNickname)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun SheetItem(
    label: String,
    icon: ImageVector,
    section: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(TestTags.otherSheetItem(section)),
    )
}
