package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.ui.common.components.ContactAvatar
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.common.components.contactAvatarLabel
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.ContactAccountUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun ContactDetailsAccountRow(
    account: ContactAccountUiModel,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.contact_details_info_from_account, account.name)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = cellShape(
            isFirst = isFirst,
            isLast = isLast,
        ),
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = Tokens.accountRowSpacing,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Tokens.rowHorizontalPadding,
                    vertical = Tokens.rowVerticalPadding,
                ),
        ) {
            Text(
                text = stringResource(R.string.contact_details_info_from),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            ContactAvatar(
                avatarImage = account.iconUri?.let(ContactAvatarImage::Uri),
                size = Tokens.accountRowAvatarSize,
                fallbackLabel = contactAvatarLabel(account.name),
            )

            Text(
                text = account.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsAccountRowPreview() {
    ContactsPreviewColumn {
        ContactDetailsAccountRow(
            account = ContactAccountUiModel(
                name = "alex@example.org",
                iconUri = null,
            ),
            isFirst = true,
            isLast = true,
        )
        ContactDetailsAccountRow(
            account = ContactAccountUiModel(
                name = "alexander.placeholder@long-example-domain.org",
                iconUri = null,
            ),
            isFirst = true,
            isLast = true,
        )
    }
}
