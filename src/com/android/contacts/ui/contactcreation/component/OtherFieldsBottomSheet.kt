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

private data class OtherFieldEntry(
    val visible: Boolean,
    val label: String,
    val icon: ImageVector,
    val section: String,
    val action: ContactCreationAction,
)

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
    val entries = buildOtherFieldEntries(
        showEvents = showEvents,
        showRelations = showRelations,
        showIm = showIm,
        showWebsites = showWebsites,
        showSip = showSip,
        showNickname = showNickname,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag(TestTags.OTHER_FIELDS_SHEET),
    ) {
        entries.filter { it.visible }.forEach { entry ->
            SheetItem(
                label = entry.label,
                icon = entry.icon,
                section = entry.section,
                onClick = {
                    onAction(entry.action)
                    onDismiss()
                },
            )
        }
    }
}

private fun buildOtherFieldEntries(
    showEvents: Boolean,
    showRelations: Boolean,
    showIm: Boolean,
    showWebsites: Boolean,
    showSip: Boolean,
    showNickname: Boolean,
): List<OtherFieldEntry> = listOf(
    OtherFieldEntry(
        showEvents,
        "Event",
        Icons.Filled.DateRange,
        "event",
        ContactCreationAction.AddEvent,
    ),
    OtherFieldEntry(
        showRelations,
        "Relation",
        Icons.Filled.People,
        "relation",
        ContactCreationAction.AddRelation,
    ),
    OtherFieldEntry(
        showIm,
        "Instant messaging",
        Icons.AutoMirrored.Filled.Message,
        "im",
        ContactCreationAction.AddIm,
    ),
    OtherFieldEntry(
        showWebsites,
        "Website",
        Icons.Filled.Public,
        "website",
        ContactCreationAction.AddWebsite,
    ),
    OtherFieldEntry(
        showSip,
        "SIP address",
        Icons.Filled.Phone,
        "sip",
        ContactCreationAction.ShowSipAddress,
    ),
    OtherFieldEntry(
        showNickname,
        "Nickname",
        Icons.Filled.Person,
        "nickname",
        ContactCreationAction.ShowNickname,
    ),
)

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
