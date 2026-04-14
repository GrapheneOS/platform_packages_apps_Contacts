package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.GroupInfo

internal fun LazyListScope.groupSection(
    availableGroups: List<GroupInfo>,
    selectedGroups: List<GroupFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    if (availableGroups.isEmpty()) return

    item(key = "group_header", contentType = "group_header") {
        Text(
            text = "Groups",
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                .testTag(TestTags.GROUP_SECTION),
        )
    }
    itemsIndexed(
        items = availableGroups,
        key = { _, group -> "group_${group.groupId}" },
        contentType = { _, _ -> "group_checkbox" },
    ) { index, group ->
        GroupCheckboxRow(
            group = group,
            isSelected = selectedGroups.any { it.groupId == group.groupId },
            index = index,
            onAction = onAction,
        )
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
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
