@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags

@Composable
internal fun AddMoreInfoSection(
    showAddressChip: Boolean,
    showOrgChip: Boolean,
    showNoteChip: Boolean,
    showGroupsChip: Boolean,
    showOtherChip: Boolean,
    onAddAddress: () -> Unit,
    onShowOrganization: () -> Unit,
    onShowNote: () -> Unit,
    onShowGroups: () -> Unit,
    onShowOtherSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.ADD_MORE_INFO_SECTION),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ChipItem(
            visible = showAddressChip,
            label = "Address",
            icon = Icons.Filled.LocationOn,
            section = "address",
            onClick = onAddAddress,
        )
        ChipItem(
            visible = showOrgChip,
            label = "Organization",
            icon = Icons.Filled.Business,
            section = "organization",
            onClick = onShowOrganization,
        )
        ChipItem(
            visible = showNoteChip,
            label = "Note",
            icon = Icons.AutoMirrored.Filled.Notes,
            section = "note",
            onClick = onShowNote,
        )
        ChipItem(
            visible = showGroupsChip,
            label = "Groups",
            icon = Icons.Filled.Group,
            section = "groups",
            onClick = onShowGroups,
        )
        ChipItem(
            visible = showOtherChip,
            label = "Other",
            icon = Icons.Filled.MoreVert,
            section = "other",
            onClick = onShowOtherSheet,
        )
    }
}

@Composable
private fun ChipItem(
    visible: Boolean,
    label: String,
    icon: ImageVector,
    section: String,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(),
    ) {
        AssistChip(
            onClick = onClick,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier.testTag(TestTags.addMoreInfoChip(section)),
        )
    }
}
