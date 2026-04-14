package com.android.contacts.ui.contactcreation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
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

internal fun LazyListScope.moreFieldsSection(
    state: MoreFieldsState,
    onAction: (ContactCreationAction) -> Unit,
) {
    moreFieldsToggle(state.isExpanded, onAction)
    moreFieldsContent(
        state.isExpanded,
        state.nickname,
        state.note,
        state.sipAddress,
        state.showSipField,
        onAction
    )
    if (state.isExpanded) {
        eventItems(state.events, onAction)
        relationItems(state.relations, onAction)
        imItems(state.imAccounts, onAction)
        websiteItems(state.websites, onAction)
    }
}

private fun LazyListScope.moreFieldsToggle(
    isExpanded: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "more_fields_toggle", contentType = "more_fields_toggle") {
        TextButton(
            onClick = { onAction(ContactCreationAction.ToggleMoreFields) },
            modifier = Modifier
                .padding(start = 16.dp)
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
                stringResource(
                    if (isExpanded) {
                        R.string.contact_creation_less_fields
                    } else {
                        R.string.contact_creation_more_fields
                    },
                ),
            )
        }
    }
}

private fun LazyListScope.moreFieldsContent(
    isExpanded: Boolean,
    nickname: String,
    note: String,
    sipAddress: String,
    showSipField: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "more_fields_content", contentType = "more_fields_content") {
        val reduceMotion = isReduceMotionEnabled()
        AnimatedVisibility(
            visible = isExpanded,
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
            MoreFieldsSingleFields(nickname, note, sipAddress, showSipField, onAction)
        }
    }
}

@Composable
private fun MoreFieldsSingleFields(
    nickname: String,
    note: String,
    sipAddress: String,
    showSipField: Boolean,
    onAction: (ContactCreationAction) -> Unit,
) {
    Column {
        OutlinedTextField(
            value = nickname,
            onValueChange = { onAction(ContactCreationAction.UpdateNickname(it)) },
            label = { Text(stringResource(R.string.nicknameLabelsGroup)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag(TestTags.NICKNAME_FIELD),
            singleLine = true,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { onAction(ContactCreationAction.UpdateNote(it)) },
            label = { Text(stringResource(R.string.contact_creation_note)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag(TestTags.NOTE_FIELD),
        )
        if (showSipField) {
            OutlinedTextField(
                value = sipAddress,
                onValueChange = { onAction(ContactCreationAction.UpdateSipAddress(it)) },
                label = { Text(stringResource(R.string.contact_creation_sip)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(TestTags.SIP_FIELD),
                singleLine = true,
            )
        }
    }
}
