package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.core.isReduceMotionEnabled

@OptIn(ExperimentalLayoutApi::class)
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
    val reduceMotion = isReduceMotionEnabled()
    val enterSpec = chipEnterTransition(reduceMotion)
    val exitSpec = chipExitTransition(reduceMotion)

    val chipItems = buildChipItems(
        showAddressChip = showAddressChip,
        showOrgChip = showOrgChip,
        showNoteChip = showNoteChip,
        showGroupsChip = showGroupsChip,
        showOtherChip = showOtherChip,
        onAddAddress = onAddAddress,
        onShowOrganization = onShowOrganization,
        onShowNote = onShowNote,
        onShowGroups = onShowGroups,
        onShowOtherSheet = onShowOtherSheet,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .testTag(TestTags.ADD_MORE_INFO_SECTION)
            .animateContentSize(
                animationSpec = if (reduceMotion) {
                    snap()
                } else {
                    spring(stiffness = Spring.StiffnessMediumLow)
                },
            ),
    ) {
        Text(
            text = stringResource(R.string.contact_creation_add_more_info),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            chipItems.forEach { item ->
                AnimatedChipSlot(
                    visible = item.visible,
                    enterSpec = enterSpec,
                    exitSpec = exitSpec,
                    label = item.label,
                    icon = item.icon,
                    section = item.section,
                    onClick = item.onClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.AnimatedChipSlot(
    visible: Boolean,
    enterSpec: EnterTransition,
    exitSpec: ExitTransition,
    label: String,
    icon: ImageVector,
    section: String,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = enterSpec,
        exit = exitSpec,
        modifier = Modifier.weight(1f),
    ) {
        ChipButton(label = label, icon = icon, section = section, onClick = onClick)
    }
}

private fun chipEnterTransition(reduceMotion: Boolean): EnterTransition {
    if (reduceMotion) return EnterTransition.None
    return expandHorizontally(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    ) + fadeIn(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
}

private fun chipExitTransition(reduceMotion: Boolean): ExitTransition {
    if (reduceMotion) return ExitTransition.None
    return shrinkHorizontally(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    ) + fadeOut(
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
}

private data class ChipItemData(
    val visible: Boolean,
    val label: String,
    val icon: ImageVector,
    val section: String,
    val onClick: () -> Unit,
)

@Composable
private fun buildChipItems(
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
): List<ChipItemData> = listOf(
    ChipItemData(
        visible = showAddressChip,
        label = stringResource(R.string.contact_creation_section_address),
        icon = Icons.Filled.LocationOn,
        section = "address",
        onClick = onAddAddress,
    ),
    ChipItemData(
        visible = showOrgChip,
        label = stringResource(R.string.contact_creation_section_organization),
        icon = Icons.Filled.Business,
        section = "organization",
        onClick = onShowOrganization,
    ),
    ChipItemData(
        visible = showNoteChip,
        label = stringResource(R.string.contact_creation_note),
        icon = Icons.AutoMirrored.Filled.Notes,
        section = "note",
        onClick = onShowNote,
    ),
    ChipItemData(
        visible = showGroupsChip,
        label = stringResource(R.string.contact_creation_groups),
        icon = Icons.Filled.Group,
        section = "groups",
        onClick = onShowGroups,
    ),
    ChipItemData(
        visible = showOtherChip,
        label = stringResource(R.string.contact_creation_other),
        icon = Icons.Filled.MoreVert,
        section = "other",
        onClick = onShowOtherSheet,
    ),
)

@Composable
private fun ChipButton(
    label: String,
    icon: ImageVector,
    section: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.addMoreInfoChip(section)),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, modifier = Modifier.padding(vertical = 10.dp))
    }
}
