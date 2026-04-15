package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.GroupInfo

/**
 * Group section as a @Composable for Column-based layout.
 * Uses FieldRow with Label icon on first row only.
 */
@Composable
internal fun GroupSectionContent(
    availableGroups: List<GroupInfo>,
    selectedGroups: List<GroupFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (availableGroups.isEmpty()) return

    Column(modifier = modifier.testTag(TestTags.GROUP_SECTION)) {
        availableGroups.forEachIndexed { index, group ->
            val isFirst = index == 0
            FieldRow(icon = if (isFirst) Icons.AutoMirrored.Filled.Label else null) {
                GroupCheckboxRow(
                    group = group,
                    isSelected = selectedGroups.any { it.groupId == group.groupId },
                    index = index,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
internal fun GroupCheckboxRow(
    group: GroupInfo,
    isSelected: Boolean,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = {
                onAction(ContactCreationAction.ToggleGroup(group.groupId, group.title))
            },
            modifier = Modifier.testTag(TestTags.groupCheckbox(index)),
        )
        Text(text = group.title)
    }
}
