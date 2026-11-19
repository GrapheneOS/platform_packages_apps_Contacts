package com.android.contacts.ui.group.detail.screen

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.model.ContactListSection
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.common.model.ContactUiModel
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
import com.android.contacts.ui.group.detail.screen.model.GroupEffect as Effect
import com.android.contacts.ui.group.detail.screen.model.GroupUiState as State
import com.android.contacts.ui.group.detail.screen.model.groupContactTestTag
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupScreenTest {

    private val actions = mutableListOf<Action>()

    // Loading

    @Test
    fun whenLoading_showsTheProgressIndicatorOnly() = runComposeUiTest {
        setContent(State.Loading)

        onNodeWithTag(GROUP_LOADING_TEST_TAG).assertExists()
        onNodeWithTag(GROUP_EMPTY_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_LIST_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_OVERFLOW_BUTTON_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenLoading_backArrowSendsDismissed() = runComposeUiTest {
        setContent(State.Loading)

        onNodeWithTag(GROUP_BACK_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.Dismissed), actions)
    }

    // Empty

    @Test
    fun whenEmpty_showsTheEmptyMessage() = runComposeUiTest {
        setContent(content(contacts = persistentMapOf()))

        onNodeWithTag(GROUP_EMPTY_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(GROUP_LOADING_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenEmpty_addContactsButtonSendsAddMembersClicked() = runComposeUiTest {
        setContent(content(contacts = persistentMapOf()))

        onNodeWithTag(GROUP_EMPTY_ADD_MEMBERS_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.AddMembersClicked), actions)
    }

    // Content

    @Test
    fun whenLoaded_showsTitleSubtitleAndContacts() = runComposeUiTest {
        setContent(content())

        onNodeWithTag(GROUP_TITLE_TEST_TAG).assertTextEquals(TITLE)
        onNodeWithTag(GROUP_SUBTITLE_TEST_TAG).assertTextEquals(SUBTITLE)
        onContact(ALICE).assertIsDisplayed()
        onContact(BOB).assertIsDisplayed()
        onNodeWithTag(GROUP_EMPTY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenSubtitleIsNull_showsOnlyTheTitle() = runComposeUiTest {
        setContent(content(subtitle = null))

        onNodeWithTag(GROUP_TITLE_TEST_TAG).assertTextEquals(TITLE)
        onNodeWithTag(GROUP_SUBTITLE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun contactClick_sendsContactClicked() = runComposeUiTest {
        setContent(content())

        onContact(BOB).performClick()

        assertEquals(listOf(Action.ContactClicked(BOB.key)), actions)
    }

    @Test
    fun contactLongClick_sendsContactLongClicked() = runComposeUiTest {
        setContent(content())

        onContact(ALICE).performTouchInput { longClick() }

        assertEquals(listOf(Action.ContactLongClicked(ALICE.key)), actions)
    }

    @Test
    fun whenNotInSelectionMode_contactsAreNotToggleable() = runComposeUiTest {
        setContent(content())

        onContactToggle(ALICE).assertDoesNotExist()
    }

    // Selection mode

    @Test
    fun whenInSelectionMode_contactsReflectTheSelection() = runComposeUiTest {
        setContent(
            content(
                contacts = contacts(ALICE, BOB.copy(isSelected = true)),
                isSelectionMode = true,
            ),
        )

        onContactToggle(ALICE).assertIsOff()
        onContactToggle(BOB).assertIsOn()
    }

    @Test
    fun whenInSelectionMode_showsCloseInsteadOfBack() = runComposeUiTest {
        setContent(content(isSelectionMode = true))

        onNodeWithTag(GROUP_BACK_BUTTON_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_CLOSE_SELECTION_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.SelectionCleared), actions)
    }

    @Test
    fun whenNotInSelectionMode_backArrowSendsDismissed() = runComposeUiTest {
        setContent(content())

        onNodeWithTag(GROUP_CLOSE_SELECTION_BUTTON_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_BACK_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.Dismissed), actions)
    }

    // Top bar actions

    @Test
    fun whenCanAddMembers_addButtonSendsAddMembersClicked() = runComposeUiTest {
        setContent(content(canAddMembers = true))

        onNodeWithTag(GROUP_ADD_MEMBERS_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.AddMembersClicked), actions)
    }

    @Test
    fun whenCannotAddMembers_addButtonIsHidden() = runComposeUiTest {
        setContent(content(canAddMembers = false))

        onNodeWithTag(GROUP_ADD_MEMBERS_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenCanRemoveMembers_removeButtonSendsRemoveSelectedClicked() = runComposeUiTest {
        setContent(content(isSelectionMode = true, canRemoveMembers = true))

        onNodeWithTag(GROUP_REMOVE_MEMBERS_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.RemoveSelectedClicked), actions)
    }

    @Test
    fun whenCannotRemoveMembers_removeButtonIsHidden() = runComposeUiTest {
        setContent(content(canRemoveMembers = false))

        onNodeWithTag(GROUP_REMOVE_MEMBERS_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenNoOverflowActions_overflowMenuIsHidden() = runComposeUiTest {
        setContent(content(canSelectAll = false, canSend = false, canEditGroup = false))

        onNodeWithTag(GROUP_OVERFLOW_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun overflowMenu_withAllFlags_showsAllActions() = runComposeUiTest {
        setContent(content(canSelectAll = true, canSend = true, canEditGroup = true))

        openOverflowMenu()

        OVERFLOW_TAGS.forEach { onNodeWithTag(it).assertIsDisplayed() }
    }

    @Test
    fun overflowMenu_withOnlySelectAll_showsOnlySelectAll() = runComposeUiTest {
        setContent(content(canSelectAll = true, canSend = false, canEditGroup = false))

        openOverflowMenu()

        assertOnlyOverflowItems(GROUP_OVERFLOW_SELECT_ALL_TEST_TAG)
    }

    @Test
    fun overflowMenu_withOnlySend_showsOnlySendActions() = runComposeUiTest {
        setContent(content(canSelectAll = false, canSend = true, canEditGroup = false))

        openOverflowMenu()

        assertOnlyOverflowItems(
            GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG,
            GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG,
        )
    }

    @Test
    fun overflowMenu_withOnlyEditGroup_showsOnlyEditActions() = runComposeUiTest {
        setContent(content(canSelectAll = false, canSend = false, canEditGroup = true))

        openOverflowMenu()

        assertOnlyOverflowItems(GROUP_OVERFLOW_RENAME_TEST_TAG, GROUP_OVERFLOW_DELETE_TEST_TAG)
    }

    @Test
    fun overflowSelectAll_sendsSelectAllClicked() = runComposeUiTest {
        assertOverflowItemSends(GROUP_OVERFLOW_SELECT_ALL_TEST_TAG, Action.SelectAllClicked)
    }

    @Test
    fun overflowSendEmail_sendsSendClickedForEmail() = runComposeUiTest {
        assertOverflowItemSends(
            GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG,
            Action.SendClicked(SendToGroupTarget.EMAIL),
        )
    }

    @Test
    fun overflowSendMessage_sendsSendClickedForSms() = runComposeUiTest {
        assertOverflowItemSends(
            GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG,
            Action.SendClicked(SendToGroupTarget.SMS),
        )
    }

    @Test
    fun overflowRename_sendsRenameClicked() = runComposeUiTest {
        assertOverflowItemSends(GROUP_OVERFLOW_RENAME_TEST_TAG, Action.RenameClicked)
    }

    @Test
    fun overflowDelete_sendsDeleteClicked() = runComposeUiTest {
        assertOverflowItemSends(GROUP_OVERFLOW_DELETE_TEST_TAG, Action.DeleteClicked)
    }

    // Floating action button

    @Test
    fun whenNotInSelectionMode_fabSendsCreateContactClicked() = runComposeUiTest {
        setContent(content())

        onNodeWithTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.CreateContactClicked), actions)
    }

    @Test
    fun whenInSelectionMode_fabIsHidden() = runComposeUiTest {
        setContent(content(isSelectionMode = true))

        onNodeWithTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenCannotAddMembers_fabIsHidden() = runComposeUiTest {
        setContent(content(canAddMembers = false))

        onNodeWithTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenLoading_fabIsHidden() = runComposeUiTest {
        setContent(State.Loading)

        onNodeWithTag(GROUP_CREATE_CONTACT_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    // Delete confirmation

    @Test
    fun whenDeleteConfirmationMessageIsNull_dialogIsHidden() = runComposeUiTest {
        setContent(content(deleteConfirmationMessage = null))

        onNodeWithTag(GROUP_DELETE_DIALOG_MESSAGE_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenDeleteConfirmationMessageIsSet_showsDialog() = runComposeUiTest {
        setContent(content(deleteConfirmationMessage = DELETE_MESSAGE))

        onNodeWithTag(GROUP_DELETE_DIALOG_MESSAGE_TEST_TAG).assertTextEquals(DELETE_MESSAGE)
        onNodeWithTag(GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(GROUP_DELETE_DIALOG_DISMISS_BUTTON_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun deleteDialogConfirm_sendsDeleteConfirmed() = runComposeUiTest {
        setContent(content(deleteConfirmationMessage = DELETE_MESSAGE))

        onNodeWithTag(GROUP_DELETE_DIALOG_CONFIRM_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.DeleteConfirmed), actions)
    }

    @Test
    fun deleteDialogCancel_sendsDeleteDismissed() = runComposeUiTest {
        setContent(content(deleteConfirmationMessage = DELETE_MESSAGE))

        onNodeWithTag(GROUP_DELETE_DIALOG_DISMISS_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.DeleteDismissed), actions)
    }

    // Screen

    @Test
    fun screen_rendersStateAndForwardsActions() = runComposeUiTest {
        val screenModel = FakeScreenModel(content())
        setScreenContent(screenModel)

        onNodeWithTag(GROUP_TITLE_TEST_TAG).assertTextEquals(TITLE)
        onContact(ALICE).performClick()

        assertEquals(listOf(Action.ContactClicked(ALICE.key)), screenModel.actions)
    }

    @Test
    fun screen_updatesWhenStateChanges() = runComposeUiTest {
        val screenModel = FakeScreenModel(State.Loading)
        setScreenContent(screenModel)

        screenModel.uiState.value = content()

        onNodeWithTag(GROUP_TITLE_TEST_TAG).assertTextEquals(TITLE)
    }

    @Test
    fun screen_passesEffectsToHandler() = runComposeUiTest {
        val effectHandler = mockk<GroupEffectHandler>(relaxed = true)
        val screenModel = FakeScreenModel(State.Loading)
        setScreenContent(screenModel, effectHandler)

        screenModel.effectsChannel.trySend(Effect.Close)
        waitForIdle()

        verify { effectHandler.handle(Effect.Close) }
    }

    @Test
    fun screen_whenInSelectionMode_backClearsSelection() = runComposeUiTest {
        val screenModel = FakeScreenModel(content(isSelectionMode = true))
        val dispatcher = setScreenContent(screenModel)

        runOnIdle { dispatcher.onBackPressed() }

        assertEquals(listOf(Action.SelectionCleared), screenModel.actions)
    }

    @Test
    fun screen_whenNotInSelectionMode_backIsNotIntercepted() = runComposeUiTest {
        val screenModel = FakeScreenModel(content(isSelectionMode = false))
        val dispatcher = setScreenContent(screenModel)

        runOnIdle { assertFalse(dispatcher.hasEnabledCallbacks()) }
    }

    private fun ComposeUiTest.setContent(state: State) {
        setContent {
            GroupContent(uiState = state, onAction = { actions += it })
        }
    }

    private fun ComposeUiTest.setScreenContent(
        screenModel: FakeScreenModel,
        effectHandler: GroupEffectHandler = mockk(relaxed = true),
    ): OnBackPressedDispatcher {
        lateinit var dispatcher: OnBackPressedDispatcher
        setContent {
            dispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            GroupScreen(
                effectHandler = effectHandler,
                snackbarHostState = SnackbarHostState(),
                screenModel = screenModel,
            )
        }
        waitForIdle()
        return dispatcher
    }

    private fun ComposeUiTest.openOverflowMenu() {
        onNodeWithTag(GROUP_OVERFLOW_BUTTON_TEST_TAG).performClick()
    }

    private fun ComposeUiTest.assertOnlyOverflowItems(vararg testTags: String) {
        OVERFLOW_TAGS.forEach { testTag ->
            val node = onNodeWithTag(testTag)
            if (testTag in testTags) node.assertIsDisplayed() else node.assertDoesNotExist()
        }
    }

    private fun ComposeUiTest.assertOverflowItemSends(testTag: String, action: Action) {
        setContent(content(canSelectAll = true, canSend = true, canEditGroup = true))

        openOverflowMenu()
        onNodeWithTag(testTag).performClick()

        assertEquals(listOf(action), actions)
        onNodeWithTag(testTag).assertDoesNotExist()
    }

    private fun ComposeUiTest.onContact(contact: ContactUiModel): SemanticsNodeInteraction {
        return onNodeWithTag(groupContactTestTag(contact.key))
    }

    // The toggleable semantics live on the cell's row, inside the tagged container
    private fun ComposeUiTest.onContactToggle(contact: ContactUiModel): SemanticsNodeInteraction {
        val cellTag = groupContactTestTag(contact.key)
        return onNode(isToggleable() and hasAnyAncestor(hasTestTag(cellTag)))
    }

    private class FakeScreenModel(
        initialState: State,
    ) : GroupScreenModel {
        val actions = mutableListOf<Action>()
        val effectsChannel = Channel<Effect>(Channel.UNLIMITED)
        override val effects: Flow<Effect> = effectsChannel.receiveAsFlow()
        override val uiState = MutableStateFlow(initialState)

        override fun onAction(action: Action) {
            actions += action
        }
    }

    private companion object {
        const val TITLE = "Family"
        const val SUBTITLE = "2 contacts"
        const val DELETE_MESSAGE = "Delete the label \"Family\"?"
        val ALICE = ContactUiModel(key = ContactItemKey.Local(1L), name = "Alice Smith")
        val BOB = ContactUiModel(key = ContactItemKey.Local(2L), name = "Bob Brown")
        val OVERFLOW_TAGS = listOf(
            GROUP_OVERFLOW_SELECT_ALL_TEST_TAG,
            GROUP_OVERFLOW_SEND_EMAIL_TEST_TAG,
            GROUP_OVERFLOW_SEND_MESSAGE_TEST_TAG,
            GROUP_OVERFLOW_RENAME_TEST_TAG,
            GROUP_OVERFLOW_DELETE_TEST_TAG,
        )

        fun contacts(vararg contacts: ContactUiModel): ContactSectionedList {
            return persistentMapOf(ContactListSection.Undefined to persistentListOf(*contacts))
        }

        @Suppress("LongParameterList")
        fun content(
            subtitle: String? = SUBTITLE,
            contacts: ContactSectionedList = contacts(ALICE, BOB),
            isSelectionMode: Boolean = false,
            canAddMembers: Boolean = true,
            canRemoveMembers: Boolean = false,
            canEditGroup: Boolean = true,
            canSend: Boolean = true,
            canSelectAll: Boolean = true,
            deleteConfirmationMessage: String? = null,
        ) = State.Content(
            title = TITLE,
            subtitle = subtitle,
            contacts = contacts,
            isSelectionMode = isSelectionMode,
            canAddMembers = canAddMembers,
            canRemoveMembers = canRemoveMembers,
            canEditGroup = canEditGroup,
            canSend = canSend,
            canSelectAll = canSelectAll,
            deleteConfirmationMessage = deleteConfirmationMessage,
        )
    }
}
