package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.ui.common.components.ContactAvatar
import com.android.contacts.ui.common.components.contactAvatarColorSeed
import com.android.contacts.ui.common.components.contactAvatarLabel
import com.android.contacts.ui.common.text.asLtrText
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.persistentListOf

private const val NAME_MAX_LINES = 3
private const val SUBTITLE_MAX_LINES = 3

@Composable
internal fun ContactDetailsHeader(
    header: ContactHeaderUiModel,
    modifier: Modifier = Modifier,
    onNameLongClick: () -> Unit = {},
    onNameBottomChanged: (Float) -> Unit = {},
) {
    val copyLabel = stringResource(R.string.copy_text)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CONTACT_DETAILS_HEADER_TEST_TAG)
            .semantics(mergeDescendants = true) {
                onLongClick(label = copyLabel) {
                    onNameLongClick()
                    true
                }
            }
            .padding(
                start = Tokens.headerHorizontalPadding,
                end = Tokens.headerHorizontalPadding,
                top = Tokens.headerTopPadding,
                bottom = Tokens.headerBottomPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderAvatar(header = header)

        Spacer(modifier = Modifier.height(Tokens.headerNameSpacing))

        HeaderName(
            text = header.displayNameText(),
            onLongClick = onNameLongClick,
            onBottomChanged = onNameBottomChanged,
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
private fun HeaderAvatar(header: ContactHeaderUiModel) {
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
}

@Composable
private fun HeaderName(
    text: String,
    onLongClick: () -> Unit,
    onBottomChanged: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = NAME_MAX_LINES,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .indication(interactionSource, ripple())
            .pointerInput(onLongClick) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                    },
                    onLongPress = { onLongClick() },
                )
            }
            .padding(
                horizontal = Tokens.headerNameHorizontalPadding,
                vertical = Tokens.headerNameVerticalPadding,
            )
            .onGloballyPositioned { coordinates ->
                onBottomChanged(coordinates.positionInRoot().y + coordinates.size.height)
            },
    )
}

@Composable
private fun HeaderSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = SUBTITLE_MAX_LINES,
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
                displayName = "Acme Support",
                subtitles = persistentListOf(),
                photo = null,
                avatarSeed = "acme-support",
                isBusiness = true,
                isDisplayNameLtr = false,
            ),
        )
    }
}
