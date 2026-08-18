package com.android.contacts.ui.contactdetails.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ContactEntryCard(
    groups: ImmutableList<ContactEntryGroupUiModel>,
    onEntryClick: (ContactEntryUiModel) -> Unit,
    onEntryCopyClick: (ContactEntryUiModel) -> Unit,
    onEntrySetDefaultClick: (ContactEntryUiModel) -> Unit,
    onEntryClearDefaultClick: (ContactEntryUiModel) -> Unit,
    onAlternateActionClick: (ContactEntryAction) -> Unit,
    onThirdActionClick: (ContactEntryAction) -> Unit,
    modifier: Modifier = Modifier,
    isTopRounded: Boolean = true,
) {
    val entries = groups.flatMap { group -> group.entries }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ContactDetailsTokens.cardSpacing),
    ) {
        entries.forEachIndexed { index, entry ->
            ContactEntryCell(
                entry = entry,
                shape = cellShape(
                    isFirst = isTopRounded && index == 0,
                    isLast = index == entries.lastIndex,
                ),
                onEntryClick = onEntryClick,
                onEntryCopyClick = onEntryCopyClick,
                onEntrySetDefaultClick = onEntrySetDefaultClick,
                onEntryClearDefaultClick = onEntryClearDefaultClick,
                onAlternateActionClick = onAlternateActionClick,
                onThirdActionClick = onThirdActionClick,
            )
        }
    }
}

@Composable
private fun ContactEntryCell(
    entry: ContactEntryUiModel,
    shape: Shape,
    onEntryClick: (ContactEntryUiModel) -> Unit,
    onEntryCopyClick: (ContactEntryUiModel) -> Unit,
    onEntrySetDefaultClick: (ContactEntryUiModel) -> Unit,
    onEntryClearDefaultClick: (ContactEntryUiModel) -> Unit,
    onAlternateActionClick: (ContactEntryAction) -> Unit,
    onThirdActionClick: (ContactEntryAction) -> Unit,
) {
    val onClick = when (entry.action) {
        null -> null
        else -> ({ onEntryClick(entry) })
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape,
    ) {
        ContactEntryRow(
            entry = entry,
            isIconVisible = true,
            onClick = onClick,
            onCopyClick = { onEntryCopyClick(entry) },
            onSetDefaultClick = { onEntrySetDefaultClick(entry) },
            onClearDefaultClick = { onEntryClearDefaultClick(entry) },
            onAlternateActionClick = onAlternateActionClick,
            onThirdActionClick = onThirdActionClick,
        )
    }
}

@PreviewLightDark
@Composable
private fun ContactEntryCardPreview() {
    ContactsPreviewColumn {
        ContactEntryCard(
            groups = persistentListOf(
                ContactEntryGroupUiModel(
                    entries = persistentListOf(
                        previewEntry(id = 1L, header = "088 525 7470", text = "Mobile"),
                        previewEntry(id = 2L, header = "088 525 7471", text = "Work"),
                    ),
                ),
                ContactEntryGroupUiModel(
                    entries = persistentListOf(
                        previewEntry(
                            id = 3L,
                            header = "anna@example.com",
                            text = "Home",
                            icon = ContactEntryIcon.EMAIL,
                        ),
                    ),
                ),
            ),
            onEntryClick = {},
            onEntryCopyClick = {},
            onEntrySetDefaultClick = {},
            onEntryClearDefaultClick = {},
            onAlternateActionClick = {},
            onThirdActionClick = {},
        )
    }
}

private fun previewEntry(
    id: Long,
    header: String,
    text: String,
    icon: ContactEntryIcon = ContactEntryIcon.CALL,
): ContactEntryUiModel {
    val alternateAction = ContactEntryAction.Sms(number = header)

    return ContactEntryUiModel(
        id = id,
        icon = icon,
        header = header,
        isHeaderLtr = true,
        subHeader = null,
        text = text,
        isSuperPrimary = false,
        isDefaultChangeable = true,
        action = ContactEntryAction.Call(number = header),
        alternateAction = when (icon) {
            ContactEntryIcon.CALL -> ContactEntryActionUiModel(
                action = alternateAction,
                icon = ContactEntryIcon.MESSAGE,
                contentDescription = "Text $header",
            )

            else -> null
        },
        thirdAction = null,
        copyText = header,
        copyLabel = text,
    )
}
