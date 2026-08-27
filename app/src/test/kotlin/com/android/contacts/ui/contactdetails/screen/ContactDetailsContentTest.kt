package com.android.contacts.ui.contactdetails.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactEntryGroupUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.tests.factory.contactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_NOTES_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTINGS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
internal class ContactDetailsContentTest {

    @Test
    fun whenLoaded_showsTheHeaderTheCardAndTheSections() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithTag(CONTACT_DETAILS_HEADER_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_NOTES_TEST_TAG).assertExists()
        onNodeWithTag(CONTACT_DETAILS_SETTINGS_TEST_TAG).assertExists()
        onNodeWithText("Notes").assertExists()
        onNodeWithText("Contact settings").assertExists()
    }

    @Test
    fun theTopBarHasNoOverflowMenu() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithContentDescription("More options").assertDoesNotExist()
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "SHARE").assertExists()
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "SHORTCUT").assertExists()
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "DELETE").assertExists()
    }

    @Test
    fun withoutAnyEntries_showsThePromptInsteadOfTheCards() = runComposeUiTest {
        val prompt = ContactDetailsEmptyPromptUiModel(
            entries = persistentListOf(
                contactEntryUiModel(id = -1L, header = "Add phone number", text = null),
            ),
        )
        val content = loadedContent(
            contactCard = persistentListOf(),
            notes = persistentListOf(),
            emptyPrompt = prompt,
        )

        setContentWith(state = State(content = content))

        onNodeWithTag(CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenThePromptIsClicked_reportsAddDetails() = runComposeUiTest {
        val prompt = ContactDetailsEmptyPromptUiModel(
            entries = persistentListOf(
                contactEntryUiModel(id = -1L, header = "Add phone number", text = null),
            ),
        )
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(
                content = loadedContent(
                    contactCard = persistentListOf(),
                    notes = persistentListOf(),
                    emptyPrompt = prompt,
                ),
            ),
            onAction = { action -> actions += action },
        )

        onNodeWithText("Add phone number").performClick()

        assertEquals(listOf(Action.AddDetailsClick), actions)
    }

    @Test
    fun whenNotFound_showsTheInvalidContactMessage() = runComposeUiTest {
        setContentWith(state = State(content = Content.NotFound))

        onNodeWithText("The contact doesn't exist.").assertIsDisplayed()
    }

    @Test
    fun whenLoading_showsNoMenu() = runComposeUiTest {
        setContentWith(state = State(content = Content.Loading))

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenStarIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).performClick()

        assertEquals(listOf(Action.StarClick), actions)
    }

    @Test
    fun whenStarred_offersRemovingFromFavorites() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(isStarred = true)))

        onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun forADirectoryContact_hidesTheHiddenMenuItems() = runComposeUiTest {
        val menu = contactDetailsMenu(
            isStarVisible = false,
            editAction = ContactDetailsEditAction.HIDDEN,
            isDeleteVisible = false,
        )

        setContentWith(
            state = State(
                content = loadedContent(
                    menu = menu,
                    settings = persistentListOf(SHARE_SETTING),
                ),
            ),
        )

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "DELETE").assertDoesNotExist()
    }

    @Test
    fun whenAMenuItemIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        scrollToSetting("SHARE")
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "SHARE").performClick()

        assertEquals(listOf(Action.ShareClick), actions)
    }

    @Test
    fun whenTheRingtoneRowIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        scrollToSetting("RINGTONE")
        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "RINGTONE").performClick()

        assertEquals(listOf(Action.RingtoneClick), actions)
    }

    @Test
    fun withoutTheRingtoneSetting_hidesTheRow() = runComposeUiTest {
        val settings = persistentListOf(SHARE_SETTING)

        setContentWith(state = State(content = loadedContent(settings = settings)))

        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "RINGTONE").assertDoesNotExist()
    }

    @Test
    fun forTheRingtoneSetting_showsTheCurrentRingtone() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithText("Bright Morning").assertExists()
    }

    @Test
    fun forAToggleSetting_showsItsState() = runComposeUiTest {
        val settings = persistentListOf(
            ContactSettingUiModel(
                icon = ContactSettingIcon.SEND_TO_VOICEMAIL,
                title = "Send to voicemail",
                subtitle = null,
                action = Action.SendToVoicemailClick,
                isDestructive = false,
                isChecked = true,
            ),
        )

        setContentWith(state = State(content = loadedContent(settings = settings)))

        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "SEND_TO_VOICEMAIL")
            .onChild()
            .assertIsOn()
    }

    @Test
    fun forAPlainSetting_showsNoToggle() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(settings = SETTINGS)))

        onNodeWithTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + "SHARE")
            .onChild()
            .assert(isToggleable().not())
    }

    @Test
    fun withoutAnySettings_hidesTheSection() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(settings = persistentListOf())))

        onNodeWithTag(CONTACT_DETAILS_SETTINGS_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenLoaded_showsTheQuickActions() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX + "CALL").assertIsEnabled()
        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX + "EMAIL").assertIsNotEnabled()
    }

    @Test
    fun whenTheHeaderIsScrolledAway_keepsTheQuickActionsPinned() = runComposeUiTest {
        val entries = (1L..20L).map { id -> contactEntryUiModel(id = id, header = "Row $id") }
        val content = loadedContent(
            contactCard = persistentListOf(contactEntryGroupUiModel(entries = entries)),
        )

        setContentWith(state = State(content = content))
        val topBeforeScroll = quickActionsTop()
        onNode(hasScrollAction()).performTouchInput { swipeUp() }

        onNodeWithTag(CONTACT_DETAILS_HEADER_TEST_TAG).assertIsNotDisplayed()
        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG).assertIsDisplayed()
        assertTrue(quickActionsTop() < topBeforeScroll)
    }

    @Test
    fun whenAQuickActionIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX + "CALL").performClick()

        assertEquals(listOf(Action.EntryClick(ContactEntryAction.Call("555 0001"))), actions)
    }

    @Test
    fun whenADisabledQuickActionIsClicked_reportsNothing() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        onNodeWithTag(CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX + "EMAIL").performClick()

        assertEquals(emptyList<Action>(), actions)
    }

    @Test
    fun whenLinking_showsTheProgressDialog() = runComposeUiTest {
        setContentWith(
            state = State(
                content = loadedContent(),
                linkProgress = ContactLinkOperation.LINK,
            ),
        )

        onNodeWithTag(CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG).assertIsDisplayed()
        onNodeWithText("Linking").assertIsDisplayed()
    }

    @Test
    fun whenNothingIsPending_showsNoProgressDialog() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithTag(CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG).assertDoesNotExist()
    }

    private fun loadedContent(
        contactCard: ImmutableList<ContactEntryGroupUiModel> = persistentListOf(
            contactEntryGroupUiModel(
                entries = listOf(contactEntryUiModel(id = 1L, header = "088 525 7470")),
            ),
        ),
        notes: ImmutableList<ContactEntryGroupUiModel> = persistentListOf(
            contactEntryGroupUiModel(
                entries = listOf(contactEntryUiModel(id = 2L, header = "Met at the airport")),
            ),
        ),
        settings: ImmutableList<ContactSettingUiModel> = SETTINGS,
        emptyPrompt: ContactDetailsEmptyPromptUiModel? = null,
        menu: ContactDetailsMenu = contactDetailsMenu(),
        isStarred: Boolean = false,
        quickActions: ImmutableList<ContactQuickActionUiModel> = QUICK_ACTIONS,
    ): Content.Loaded {
        return Content.Loaded(
            recentCalls = persistentListOf(),
            groups = persistentListOf(),
            header = contactHeaderUiModel(displayName = "Anna Smith"),
            quickActions = quickActions,
            contactCard = contactCard,
            notes = notes,
            settings = settings,
            emptyPrompt = emptyPrompt,
            menu = menu,
            isStarred = isStarred,
        )
    }

    private fun ComposeUiTest.scrollToSetting(name: String) {
        onNode(hasScrollAction()).performScrollToNode(
            hasTestTag(CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + name),
        )
    }

    private fun ComposeUiTest.quickActionsTop(): Dp {
        return onNodeWithTag(CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG)
            .getUnclippedBoundsInRoot()
            .top
    }

    private fun ComposeUiTest.setContentWith(
        state: State,
        onAction: (Action) -> Unit = {},
    ) {
        setContent {
            ContactDetailsContent(
                uiState = state,
                onAction = onAction,
            )
        }
    }

    private companion object {
        val SHARE_SETTING = ContactSettingUiModel(
            icon = ContactSettingIcon.SHARE,
            title = "Share",
            subtitle = null,
            action = Action.ShareClick,
            isDestructive = false,
            isChecked = null,
        )

        val SETTINGS = persistentListOf(
            ContactSettingUiModel(
                icon = ContactSettingIcon.RINGTONE,
                title = "Set ringtone",
                subtitle = "Bright Morning",
                action = Action.RingtoneClick,
                isDestructive = false,
                isChecked = null,
            ),
            SHARE_SETTING,
            ContactSettingUiModel(
                icon = ContactSettingIcon.SHORTCUT,
                title = "Create shortcut",
                subtitle = null,
                action = Action.ShortcutClick,
                isDestructive = false,
                isChecked = null,
            ),
            ContactSettingUiModel(
                icon = ContactSettingIcon.DELETE,
                title = "Delete",
                subtitle = null,
                action = Action.DeleteClick,
                isDestructive = true,
                isChecked = null,
            ),
        )

        val QUICK_ACTIONS = persistentListOf(
            contactQuickActionUiModel(icon = ContactEntryIcon.CALL, label = "Call"),
            contactQuickActionUiModel(
                icon = ContactEntryIcon.EMAIL,
                label = "Email",
                action = null,
            ),
        )
    }
}
