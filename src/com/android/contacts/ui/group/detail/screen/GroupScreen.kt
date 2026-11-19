@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.contacts.ui.group.detail.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.common.components.ContactSectionedListColumn
import com.android.contacts.ui.common.components.OverflowMenu
import com.android.contacts.ui.common.components.OverflowMenuItem
import com.android.contacts.ui.common.components.delayedIndicator
import com.android.contacts.ui.core.CollectEvents
import com.android.contacts.ui.core.scrollingScreenPadding
import com.android.contacts.ui.group.detail.screen.model.GROUP_ADD_MEMBERS_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_BACK_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_CLOSE_SELECTION_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_CREATE_CONTACT_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_DELETE_DIALOG_DISMISS_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_DELETE_DIALOG_MESSAGE_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_EMPTY_ADD_MEMBERS_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_EMPTY_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_LIST_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_LOADING_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_DELETE_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_RENAME_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_SELECT_ALL_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_REMOVE_MEMBERS_BUTTON_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_SUBTITLE_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GROUP_TITLE_TEST_TAG
import com.android.contacts.ui.group.detail.screen.model.GroupAction as Action
import com.android.contacts.ui.group.detail.screen.model.GroupUiState as State
import com.android.contacts.ui.group.detail.screen.model.groupContactTestTag
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget

@Composable
internal fun GroupScreen(
    effectHandler: GroupEffectHandler,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    screenModel: GroupScreenModel = viewModel<GroupViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    CollectEvents(screenModel.effects, effectHandler::handle)

    GroupContent(
        uiState = uiState,
        onAction = screenModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )

    BackHandler(enabled = (uiState as? State.Content)?.isSelectionMode == true) {
        screenModel.onAction(Action.SelectionCleared)
    }
}

@Composable
internal fun GroupContent(
    uiState: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val content = uiState as? State.Content

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            GroupTopAppBar(
                content = content,
                onAction = onAction,
            )
        },
        floatingActionButton = {
            CreateContactButton(
                isVisible = content != null && !content.isSelectionMode && content.canAddMembers,
                onClick = { onAction(Action.CreateContactClicked) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { contentPadding ->
        Box(
            Modifier
                .padding(scrollingScreenPadding(contentPadding))
                .consumeWindowInsets(contentPadding)
                .fillMaxSize(),
        ) {
            when {
                content == null -> {
                    GroupLoading()
                }
                content.contacts.isEmpty() -> {
                    GroupEmpty(
                        canAddMembers = content.canAddMembers,
                        onAddMembersClick = { onAction(Action.AddMembersClicked) },
                    )
                }
                else -> {
                    ContactSectionedListColumn(
                        contactSections = content.contacts,
                        isSelectable = content.isSelectionMode,
                        showAvatars = true,
                        onContactClick = { onAction(Action.ContactClicked(it)) },
                        onContactLongClick = { onAction(Action.ContactLongClicked(it)) },
                        contentPadding = contentPadding,
                        contactModifier = { _, key ->
                            Modifier.testTag(groupContactTestTag(key))
                        },
                        modifier = Modifier.testTag(GROUP_LIST_TEST_TAG),
                    )
                }
            }
        }
    }

    content?.deleteConfirmationMessage?.let { message ->
        DeleteConfirmationDialog(
            message = message,
            onConfirm = { onAction(Action.DeleteConfirmed) },
            onDismiss = { onAction(Action.DeleteDismissed) },
        )
    }
}

@Composable
private fun GroupTopAppBar(
    content: State.Content?,
    onAction: (Action) -> Unit,
) {
    val isSelectionMode = content?.isSelectionMode == true

    TopAppBar(
        navigationIcon = {
            when {
                isSelectionMode -> IconButton(
                    onClick = { onAction(Action.SelectionCleared) },
                    modifier = Modifier.testTag(GROUP_CLOSE_SELECTION_BUTTON_TEST_TAG),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(
                            R.string.cancel_button_content_description,
                        ),
                    )
                }
                else -> IconButton(
                    onClick = { onAction(Action.Dismissed) },
                    modifier = Modifier.testTag(GROUP_BACK_BUTTON_TEST_TAG),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(
                            R.string.back_arrow_content_description,
                        ),
                    )
                }
            }
        },
        title = {
            if (content != null) {
                GroupTitle(title = content.title, subtitle = content.subtitle)
            }
        },
        actions = {
            if (content != null) {
                GroupActions(content = content, onAction = onAction)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun GroupTitle(
    title: String,
    subtitle: String?,
) {
    Column {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag(GROUP_TITLE_TEST_TAG),
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(GROUP_SUBTITLE_TEST_TAG),
            )
        }
    }
}

@Composable
private fun GroupActions(
    content: State.Content,
    onAction: (Action) -> Unit,
) {
    if (content.canAddMembers) {
        IconButton(
            onClick = { onAction(Action.AddMembersClicked) },
            modifier = Modifier.testTag(GROUP_ADD_MEMBERS_BUTTON_TEST_TAG),
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAddAlt,
                contentDescription = stringResource(R.string.menu_addContactsToGroup),
            )
        }
    }
    if (content.canRemoveMembers) {
        IconButton(
            onClick = { onAction(Action.RemoveSelectedClicked) },
            modifier = Modifier.testTag(GROUP_REMOVE_MEMBERS_BUTTON_TEST_TAG),
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonRemove,
                contentDescription = stringResource(R.string.menu_removeFromGroup),
            )
        }
    }
    val overflowActions = buildOverflowActions(content)
    if (overflowActions.isNotEmpty()) {
        OverflowMenu(modifier = Modifier.testTag(GROUP_OVERFLOW_BUTTON_TEST_TAG)) { dismiss ->
            overflowActions.onEach { overflowAction ->
                OverflowMenuItem(
                    labelResId = overflowAction.labelResId,
                    onClick = {
                        dismiss()
                        onAction(overflowAction.action)
                    },
                    modifier = Modifier.testTag(overflowAction.testTag),
                )
            }
        }
    }
}

@Composable
private fun buildOverflowActions(content: State.Content): List<OverflowAction> {
    return remember(content.canSelectAll, content.canSend, content.canEditGroup) {
        buildList {
            if (content.canSelectAll) {
                add(
                    OverflowAction(
                        labelResId = R.string.menu_select_all,
                        testTag = GROUP_OVERFLOW_SELECT_ALL_TEST_TAG,
                        action = Action.SelectAllClicked,
                    ),
                )
            }
            if (content.canSend) {
                add(
                    OverflowAction(
                        labelResId = R.string.menu_sendEmailOption,
                        testTag = GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG,
                        action = Action.SendClicked(SendToGroupTarget.EMAIL),
                    ),
                )
                add(
                    OverflowAction(
                        labelResId = R.string.menu_sendMessageOption,
                        testTag = GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG,
                        action = Action.SendClicked(SendToGroupTarget.SMS),
                    ),
                )
            }
            if (content.canEditGroup) {
                add(
                    OverflowAction(
                        labelResId = R.string.menu_renameGroup,
                        testTag = GROUP_OVERFLOW_RENAME_TEST_TAG,
                        action = Action.RenameClicked,
                    ),
                )
                add(
                    OverflowAction(
                        labelResId = R.string.menu_deleteGroup,
                        testTag = GROUP_OVERFLOW_DELETE_TEST_TAG,
                        action = Action.DeleteClicked,
                    ),
                )
            }
        }
    }
}

private data class OverflowAction(
    val labelResId: Int,
    val testTag: String,
    val action: Action,
)

@Composable
private fun CreateContactButton(
    isVisible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.testTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(
                    R.string.action_menu_add_new_contact_button,
                ),
            )
        }
    }
}

@Composable
private fun GroupLoading() {
    LinearProgressIndicator(
        modifier = Modifier
            .height(2.dp)
            .fillMaxWidth()
            .delayedIndicator()
            .testTag(GROUP_LOADING_TEST_TAG),
    )
}

@Composable
private fun GroupEmpty(
    canAddMembers: Boolean,
    onAddMembersClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag(GROUP_EMPTY_TEST_TAG),
    ) {
        Image(
            painter = painterResource(R.drawable.label_empty),
            contentDescription = null,
        )

        Spacer(Modifier.size(32.dp))

        Text(
            text = stringResource(R.string.emptyGroup),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        if (canAddMembers) {
            Spacer(Modifier.size(8.dp))

            TextButton(
                onClick = onAddMembersClick,
                modifier = Modifier.testTag(GROUP_EMPTY_ADD_MEMBERS_BUTTON_TEST_TAG),
            ) {
                Text(
                    text = stringResource(R.string.menu_addContactsToGroup),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = message,
                modifier = Modifier.testTag(GROUP_DELETE_DIALOG_MESSAGE_TEST_TAG),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG),
            ) {
                Text(stringResource(R.string.deleteConfirmation_positive_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(GROUP_DELETE_DIALOG_DISMISS_BUTTON_TEST_TAG),
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
