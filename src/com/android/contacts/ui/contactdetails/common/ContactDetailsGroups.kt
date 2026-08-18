package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_GROUPS_TEST_TAG
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens

@Composable
internal fun ContactDetailsGroups(
    groups: ImmutableList<String>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Tokens.groupChipSpacing),
        modifier = modifier
            .testTag(CONTACT_DETAILS_GROUPS_TEST_TAG)
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        groups.forEach { group ->
            SuggestionChip(
                onClick = {},
                modifier = Modifier.clearAndSetSemantics { contentDescription = group },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Label,
                        contentDescription = null,
                        modifier = Modifier.size(Tokens.groupChipIconSize),
                    )
                },
                label = {
                    Text(
                        text = group,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsGroupsPreview() {
    ContactsPreviewColumn {
        ContactDetailsGroups(
            groups = persistentListOf("Coworkers", "Family", "Friends"),
            contentPadding = PaddingValues(),
        )
    }
}
