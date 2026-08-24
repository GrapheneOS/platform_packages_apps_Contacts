package com.android.contacts.ui.contactdetails.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EDIT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_TITLE_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.core.ContactsPreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactDetailsTopAppBar(
    title: String,
    isTitleVisible: Boolean,
    menu: ContactDetailsMenu?,
    isStarred: Boolean,
    onAction: (Action) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {
            AnimatedVisibility(
                visible = isTitleVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(CONTACT_DETAILS_TITLE_TEST_TAG),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onAction(Action.BackClick) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_arrow_content_description),
                )
            }
        },
        actions = {
            if (menu != null) {
                StarAction(
                    menu = menu,
                    isStarred = isStarred,
                    onClick = { onAction(Action.StarClick) },
                )

                EditAction(
                    editAction = menu.editAction,
                    onClick = { onAction(Action.EditClick) },
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

@Composable
private fun StarAction(
    menu: ContactDetailsMenu,
    isStarred: Boolean,
    onClick: () -> Unit,
) {
    if (!menu.isStarVisible) {
        return
    }

    val label = when {
        isStarred -> R.string.menu_removeStar
        else -> R.string.menu_addStar
    }

    IconToggleButton(
        checked = isStarred,
        onCheckedChange = { onClick() },
        modifier = Modifier.testTag(CONTACT_DETAILS_STAR_TEST_TAG),
    ) {
        Icon(
            imageVector = when {
                isStarred -> Icons.Rounded.Star
                else -> Icons.Rounded.StarBorder
            },
            contentDescription = stringResource(label),
        )
    }
}

@Composable
private fun EditAction(
    editAction: ContactDetailsEditAction,
    onClick: () -> Unit,
) {
    if (editAction == ContactDetailsEditAction.HIDDEN) {
        return
    }

    val isAdd = editAction == ContactDetailsEditAction.ADD
    val label = when {
        isAdd -> R.string.menu_add_contact
        else -> R.string.menu_editContact
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(CONTACT_DETAILS_EDIT_TEST_TAG),
    ) {
        Icon(
            imageVector = when {
                isAdd -> Icons.Rounded.PersonAdd
                else -> Icons.Rounded.Edit
            },
            contentDescription = stringResource(label),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun ContactDetailsTopAppBarPreview() {
    ContactsPreviewTheme {
        ContactDetailsTopAppBar(
            title = "Anna Smith",
            isTitleVisible = true,
            menu = ContactDetailsMenu(
                isStarVisible = true,
                editAction = ContactDetailsEditAction.EDIT,
                isJoinVisible = true,
                isLinkedContactsVisible = false,
                isDeleteVisible = true,
                isShareVisible = true,
                isShortcutVisible = true,
                isRingtoneVisible = true,
                isSendToVoicemailVisible = true,
            ),
            isStarred = true,
            onAction = {},
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        )
    }
}
