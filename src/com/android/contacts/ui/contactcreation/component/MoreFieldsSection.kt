package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.core.isReduceMotionEnabled

/**
 * More fields section as a @Composable for Column-based layout.
 * TextButton toggle at 56dp start, binary expand/collapse.
 */
@Composable
internal fun MoreFieldsSectionContent(
    state: MoreFieldsState,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MoreFieldsToggleButton(
            isExpanded = state.isExpanded,
            onAction = onAction,
        )

        val reduceMotion = isReduceMotionEnabled()
        AnimatedVisibility(
            visible = state.isExpanded,
            enter = if (reduceMotion) {
                expandVertically() + fadeIn()
            } else {
                expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ) + fadeIn()
            },
            exit = if (reduceMotion) {
                shrinkVertically() + fadeOut()
            } else {
                shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                ) + fadeOut()
            },
            modifier = Modifier.testTag(TestTags.MORE_FIELDS_CONTENT),
        ) {
            MoreFieldsExpandedContent(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun MoreFieldsToggleButton(
    isExpanded: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    TextButton(
        onClick = { onAction(ContactCreationAction.ToggleMoreFields) },
        modifier = Modifier
            .padding(start = 56.dp)
            .testTag(TestTags.MORE_FIELDS_TOGGLE),
    ) {
        Icon(
            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = stringResource(
                if (isExpanded) {
                    R.string.contact_creation_less_fields
                } else {
                    R.string.contact_creation_more_fields
                },
            ),
        )
        Text(
            text = stringResource(
                if (isExpanded) {
                    R.string.contact_creation_less_fields
                } else {
                    R.string.contact_creation_more_fields
                },
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun MoreFieldsExpandedContent(
    state: MoreFieldsState,
    onAction: (ContactCreationAction) -> Unit,
) {
    Column {
        // 1. Nickname (single field, no header needed)
        NicknameField(nickname = state.nickname, onAction = onAction)

        // 2. Organization
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Organization")
        OrganizationSectionContent(organization = state.organization, onAction = onAction)

        // 3. SIP (single field — icon identifies it, no header needed)
        if (state.showSipField) {
            Spacer(modifier = Modifier.height(24.dp))
            SipField(sipAddress = state.sipAddress, onAction = onAction)
        }

        // 4. IM
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Instant messaging")
        ImSectionContent(imAccounts = state.imAccounts, onAction = onAction)

        // 5. Website
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Websites")
        WebsiteSectionContent(websites = state.websites, onAction = onAction)

        // 6. Event
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Events")
        EventSectionContent(events = state.events, onAction = onAction)

        // 7. Relation
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Relations")
        RelationSectionContent(relations = state.relations, onAction = onAction)

        // 8. Note (single field — icon identifies it, no header needed)
        Spacer(modifier = Modifier.height(24.dp))
        NoteField(note = state.note, onAction = onAction)
    }
}

@Composable
private fun NicknameField(
    nickname: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow(icon = null) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { onAction(ContactCreationAction.UpdateNickname(it)) },
            label = { Text(stringResource(R.string.nicknameLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NICKNAME_FIELD),
            singleLine = true,
        )
    }
}

@Composable
private fun NoteField(
    note: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow(icon = Icons.Filled.Notes) {
        OutlinedTextField(
            value = note,
            onValueChange = { onAction(ContactCreationAction.UpdateNote(it)) },
            label = { Text(stringResource(R.string.contact_creation_note)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.NOTE_FIELD),
            singleLine = false,
            maxLines = 4,
        )
    }
}

@Composable
private fun SipField(
    sipAddress: String,
    onAction: (ContactCreationAction) -> Unit,
) {
    FieldRow(icon = Icons.Filled.DialerSip) {
        OutlinedTextField(
            value = sipAddress,
            onValueChange = { onAction(ContactCreationAction.UpdateSipAddress(it)) },
            label = { Text(stringResource(R.string.contact_creation_sip)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.SIP_FIELD),
            singleLine = true,
        )
    }
}
