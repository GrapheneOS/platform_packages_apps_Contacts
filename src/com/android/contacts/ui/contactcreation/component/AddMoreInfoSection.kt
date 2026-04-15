package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags

private data class ItemData(
    val label: String,
    val icon: ImageVector,
    val section: String,
    val onClick: () -> Unit,
)

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
    val items = buildList {
        if (showAddressChip) {
            add(ItemData("Address", Icons.Filled.LocationOn, "address", onAddAddress))
        }
        if (showOrgChip) {
            add(ItemData("Organization", Icons.Filled.Business, "organization", onShowOrganization))
        }
        if (showNoteChip) {
            add(ItemData("Note", Icons.AutoMirrored.Filled.Notes, "note", onShowNote))
        }
        if (showGroupsChip) {
            add(ItemData("Groups", Icons.Filled.Group, "groups", onShowGroups))
        }
        if (showOtherChip) {
            add(ItemData("Other", Icons.Filled.MoreVert, "other", onShowOtherSheet))
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .testTag(TestTags.ADD_MORE_INFO_SECTION),
    ) {
        Text(
            text = "Add more info",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        items.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                pair.forEach { item ->
                    FilledTonalButton(
                        onClick = item.onClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(TestTags.addMoreInfoChip(item.section)),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(item.label, modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
