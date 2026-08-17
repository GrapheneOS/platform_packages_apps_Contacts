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
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.collections.immutable.persistentListOf
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens

@Composable
internal fun ContactDetailsHeader(
    header: ContactHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    val displayName = header.displayNameText()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_HEADER_TEST_TAG)
            .semantics(mergeDescendants = true) {}
            .padding(
                start = Tokens.headerHorizontalPadding,
                end = Tokens.headerHorizontalPadding,
                top = Tokens.headerTopPadding,
                bottom = Tokens.headerBottomPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ContactAvatar(
            avatarImage = header.photo,
            size = Tokens.headerAvatarSize,
            fallbackLabel = contactAvatarLabel(header.displayName),
            colorSeed = contactAvatarColorSeed(header.avatarSeed),
            fallbackIcon = when {
                header.isBusiness -> Icons.Rounded.Business
                else -> Icons.Rounded.Person
            },
        )

        Spacer(modifier = Modifier.height(Tokens.headerNameSpacing))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )

        if (header.subtitles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Tokens.headerSubtitleSpacing))

            header.subtitles.forEach { subtitle ->
                HeaderSubtitle(text = subtitle)
            }
        }
    }
}

@Composable
private fun HeaderSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}

@Composable
private fun ContactHeaderUiModel.displayNameText(): String {
    return when {
        isDisplayNameLtr -> displayName.asLtrText()
        else -> displayName
    }
}

@PreviewLightDark
@Composable
private fun ContactDetailsHeaderPreview() {
    ContactsPreviewColumn {
        ContactDetailsHeader(
            header = ContactHeaderUiModel(
                displayName = "Anna Smith",
                subtitles = persistentListOf("Anna Sumisu", "Annie", "Acme"),
                photo = null,
                avatarSeed = "anna-smith",
                isBusiness = false,
                isDisplayNameLtr = false,
            ),
        )
        ContactDetailsHeader(
            header = ContactHeaderUiModel(
                displayName = "Student Administration",
                subtitles = persistentListOf(),
                photo = null,
                avatarSeed = "student-administration",
                isBusiness = true,
                isDisplayNameLtr = false,
            ),
        )
    }
}
