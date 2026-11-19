package com.android.contacts.ui.selection.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.components.ContactSectionedListColumn
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.common.util.messageFormatResource
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_LIST_HEADER_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_LIST_TEST_TAG
import com.android.contacts.ui.selection.screen.model.contactSelectionContactTestTag
import com.android.contacts.ui.selection.screen.model.contactSelectionSectionTestTag
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun ContactSelectionList(
    header: String?,
    contactSections: ContactSectionedList,
    canCreate: Boolean,
    allowMultiple: Boolean,
    showAvatars: Boolean,
    onCreateClick: () -> Unit,
    onContactClick: (ContactItemKey) -> Unit,
    contentPadding: PaddingValues,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    ContactSectionedListColumn(
        contactSections = contactSections,
        isSelectable = allowMultiple,
        showAvatars = showAvatars,
        onContactClick = onContactClick,
        contentPadding = contentPadding,
        lazyListState = lazyListState,
        sectionModifier = { section ->
            Modifier.testTag(contactSelectionSectionTestTag(section))
        },
        contactModifier = { section, key ->
            Modifier.testTag(contactSelectionContactTestTag(section, key))
        },
        modifier = Modifier.testTag(CONTACT_SELECTION_LIST_TEST_TAG),
        leadingContent = {
            header?.let {
                item("header") {
                    HeaderText(it)
                }
            }
            if (canCreate) {
                item("create") {
                    ContactCreateButton(onClick = onCreateClick)
                }
            }
        },
    )
}

@Composable
private fun HeaderText(listHeader: String) {
    Text(
        text = listHeader,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .testTag(CONTACT_SELECTION_LIST_HEADER_TEST_TAG)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun ContactCreateButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .testTag(CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG)
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAddAlt,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.header_entry_contact_list_adapter_header_title))
        }
    }
}

@PreviewLightDark
@Composable
private fun HeaderTextPreview() {
    ContactsPreviewColumn {
        HeaderText(
            messageFormatResource(
                R.string.contacts_count_with_account,
                persistentMapOf(
                    "count" to 123,
                    "account" to "user@example.org",
                )
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactCreateButtonPreview() {
    ContactsPreviewColumn {
        ContactCreateButton({})
    }
}
