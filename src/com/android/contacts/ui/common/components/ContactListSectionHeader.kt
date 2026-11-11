package com.android.contacts.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.selection.screen.model.ContactListSection

@Composable
internal fun ContactListSectionHeader(
    section: ContactListSection,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics { heading() }
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        if (section == ContactListSection.Favorites) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = when (section) {
                ContactListSection.Suggestions ->
                    stringResource(R.string.separatorJoinAggregateSuggestions)
                ContactListSection.Favorites ->
                    stringResource(R.string.contactsFavoritesLabel)
                is ContactListSection.Alphabetical ->
                    section.letter
                is ContactListSection.Directory ->
                    section.name
                ContactListSection.Undefined ->
                    ""
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (section is ContactListSection.Directory && section.isLoading) {
            Text(
                text = stringResource(R.string.search_results_searching),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactListSectionHeaderPreview() {
    ContactsPreviewColumn {
        listOf(
            ContactListSection.Suggestions,
            ContactListSection.Favorites,
            ContactListSection.Alphabetical("A"),
            ContactListSection.Alphabetical("B"),
            ContactListSection.Alphabetical("#"),
            ContactListSection.Directory(1L, "Corporate"),
            ContactListSection.Directory(2L, "Corporate", isLoading = true),
        ).forEach { ContactListSectionHeader(it) }
    }
}
