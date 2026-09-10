package com.android.contacts.ui.editor.springboard.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.common.components.AccountIcon
import com.android.contacts.ui.common.components.ContactAvatar
import com.android.contacts.ui.common.components.contactAvatarColorSeed
import com.android.contacts.ui.common.components.contactAvatarLabel
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.editor.springboard.screen.model.CONTACT_EDITOR_SB_RAW_CONTACT_TEST_TAG_PREFIX
import com.android.contacts.ui.editor.springboard.screen.model.RawContactUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun RawContactsSelectionList(
    contacts: ImmutableList<RawContactUiModel>,
    onContactPicked: (Long) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            RawContactCell(
                contact = contact,
                onClick = { onContactPicked(contact.id) },
            )
        }
    }
}

@Composable
private fun RawContactCell(
    contact: RawContactUiModel,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(CONTACT_EDITOR_SB_RAW_CONTACT_TEST_TAG_PREFIX + contact.id),
    ) {
        ContactAvatar(
            avatarImage = contact.avatarImage,
            size = 40.dp,
            fallbackLabel = contactAvatarLabel(contact.displayName),
            colorSeed = contactAvatarColorSeed(contact.displayName),
            fallbackIcon = Icons.Rounded.Person,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = contact.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccountIcon(
                    data = contact.accountIconData,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(16.dp),
                )
                Text(
                    text = contact.accountLabel.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RawContactsSelectionListPreview() {
    ContactsPreviewColumn {
        RawContactsSelectionList(
            contacts = persistentListOf(
                buildRawContactForPreview(1L),
                buildRawContactForPreview(2L),
                buildRawContactForPreview(3L),
            ),
            onContactPicked = {},
        )
    }
}

private fun buildRawContactForPreview(
    id: Long,
): RawContactUiModel {
    return RawContactUiModel(
        id = id,
        avatarImage = null,
        displayName = "John Smith",
        accountLabel = "Device",
        accountIconData = null,
    )
}
