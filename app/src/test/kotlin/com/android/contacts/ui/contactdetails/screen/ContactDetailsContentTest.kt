package com.android.contacts.ui.contactdetails.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onChild
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
import com.android.contacts.tests.factory.contactDetailsLoadedContent
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactEntryGroupUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.tests.factory.contactQuickActionUiModel
import com.android.contacts.tests.factory.contactSettingUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ACCOUNTS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_NOTES_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTINGS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactConnectedAppUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactGroupUiModel
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
    fun withConnectedApps_showsTheAppsAndKeepsTheirRowsHidden() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(connectedApps = CONNECTED_APPS)))

        scrollTo(CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG)

        onNodeWithText("Connected apps").assertExists()
        onNodeWithText("Chat").assertIsDisplayed()
        onNodeWithText("Message 088 525 7470").assertDoesNotExist()
    }

    @Test
    fun whenAConnectedAppIsTapped_showsItsRows() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(connectedApps = CONNECTED_APPS)))

        scrollTo(CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG)
        onNodeWithText("Chat").performClick()

        onNodeWithText("Message 088 525 7470").assertIsDisplayed()
    }

    @Test
    fun whenAnOpenConnectedAppIsTapped_hidesItsRowsAgain() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent(connectedApps = CONNECTED_APPS)))

        scrollTo(CONTACT_DETAILS_CONNECTED_APPS_TEST_TAG)
        onNodeWithText("Chat").performClick()
        onNodeWithText("Chat").performClick()

        onNodeWithText("Message 088 525 7470").assertDoesNotExist()
    }

    @Test
    fun withAnAccount_showsWhereTheContactInfoComesFrom() = runComposeUiTest {
        val content = loadedContent(
            accounts = persistentListOf(
                ContactAccountUiModel(name = "alex@example.org", iconUri = null),
            ),
        )

        setContentWith(state = State(content = content))
        scrollTo(CONTACT_DETAILS_ACCOUNTS_TEST_TAG)
        onNodeWithContentDescription("Contact info from alex@example.org").assertIsDisplayed()
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
        val content = loadedContent(
            contactCard = persistentListOf(),
            notes = persistentListOf(),
            emptyPrompt = EMPTY_PROMPT,
        )

        setContentWith(state = State(content = content))

        onNodeWithTag(CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenAGroupChipIsClicked_reportsTheGroupId() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(
                content = loadedContent(
                    groups = persistentListOf(ContactGroupUiModel(id = 11L, title = "Coworkers")),
                ),
            ),
            onAction = { action -> actions += action },
        )

        onNodeWithContentDescription("Coworkers").performClick()

        assertEquals(listOf(Action.GroupClick(11L)), actions)
    }

    @Test
    fun whenThePromptIsClicked_reportsAddDetails() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(
                content = loadedContent(
                    contactCard = persistentListOf(),
                    notes = persistentListOf(),
                    emptyPrompt = EMPTY_PROMPT,
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
            contactSettingUiModel(
                icon = ContactSettingIcon.SEND_TO_VOICEMAIL,
                title = "Send to voicemail",
                action = Action.SendToVoicemailClick,
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
        connectedApps: ImmutableList<ContactConnectedAppUiModel> = persistentListOf(),
        groups: ImmutableList<ContactGroupUiModel> = persistentListOf(),
        accounts: ImmutableList<ContactAccountUiModel> = persistentListOf(),
    ): Content.Loaded {
        return contactDetailsLoadedContent(
            header = contactHeaderUiModel(displayName = "Anna Smith"),
            quickActions = quickActions,
            groups = groups,
            contactCard = contactCard,
            connectedApps = connectedApps,
            notes = notes,
            settings = settings,
            accounts = accounts,
            emptyPrompt = emptyPrompt,
            menu = menu,
            isStarred = isStarred,
        )
    }

    private fun ComposeUiTest.scrollTo(testTag: String) {
        onNode(hasScrollAction()).performScrollToNode(hasTestTag(testTag))
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
        val CONNECTED_APPS = persistentListOf(
            ContactConnectedAppUiModel(
                packageName = "com.example.chat",
                label = "Chat",
                iconUri = null,
                entries = persistentListOf(
                    contactEntryUiModel(
                        id = 3L,
                        header = "Message 088 525 7470",
                        text = null,
                    ),
                ),
            ),
        )

        val SHARE_SETTING = contactSettingUiModel(
            icon = ContactSettingIcon.SHARE,
            title = "Share",
            action = Action.ShareClick,
        )

        val SETTINGS = persistentListOf(
            contactSettingUiModel(
                icon = ContactSettingIcon.RINGTONE,
                title = "Set ringtone",
                subtitle = "Bright Morning",
                action = Action.RingtoneClick,
            ),
            SHARE_SETTING,
            contactSettingUiModel(
                icon = ContactSettingIcon.SHORTCUT,
                title = "Create shortcut",
                action = Action.ShortcutClick,
            ),
            contactSettingUiModel(
                icon = ContactSettingIcon.DELETE,
                title = "Delete",
                action = Action.DeleteClick,
                isDestructive = true,
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

        val EMPTY_PROMPT = ContactDetailsEmptyPromptUiModel(
            entries = persistentListOf(
                contactEntryUiModel(id = -1L, header = "Add phone number", text = null),
            ),
        )
    }
}
