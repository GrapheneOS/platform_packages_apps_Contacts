package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.ui.common.components.ContactAvatar
import com.android.contacts.ui.common.components.contactAvatarColorSeed
import com.android.contacts.ui.common.components.contactAvatarLabel
import com.android.contacts.ui.common.text.asLtrText
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn

@Composable
internal fun ContactDetailsHeader(
    header: ContactHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_HEADER_TEST_TAG)
            .semantics(mergeDescendants = true) {}
            .padding(
                start = ContactDetailsTokens.headerHorizontalPadding,
                end = ContactDetailsTokens.headerHorizontalPadding,
                top = ContactDetailsTokens.headerTopPadding,
                bottom = ContactDetailsTokens.headerBottomPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ContactAvatar(
            avatarImage = header.photo,
            size = ContactDetailsTokens.headerAvatarSize,
            fallbackLabel = contactAvatarLabel(header.displayName),
            colorSeed = contactAvatarColorSeed(header.avatarSeed),
            fallbackIcon = when {
                header.isBusiness -> Icons.Rounded.Business
                else -> Icons.Rounded.Person
            },
        )

        Spacer(modifier = Modifier.height(ContactDetailsTokens.headerNameSpacing))

        Text(
            text = header.displayName.asLtrText(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )

        val phoneticName = header.phoneticName
        if (phoneticName != null) {
            Text(
                text = phoneticName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.padding(top = ContactDetailsTokens.headerPhoneticSpacing),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsHeaderPreview() {
    ContactsPreviewColumn {
        ContactDetailsHeader(
            header = ContactHeaderUiModel(
                displayName = "Anna Smith",
                phoneticName = "Anna Sumisu",
                photo = null,
                avatarSeed = "anna-smith",
                isBusiness = false,
            ),
        )
        ContactDetailsHeader(
            header = ContactHeaderUiModel(
                displayName = "Student Administration",
                phoneticName = null,
                photo = null,
                avatarSeed = "student-administration",
                isBusiness = true,
            ),
        )
    }
}
