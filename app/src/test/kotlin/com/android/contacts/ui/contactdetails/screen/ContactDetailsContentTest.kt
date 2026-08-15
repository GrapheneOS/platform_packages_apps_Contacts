package com.android.contacts.ui.contactdetails.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactEntryGroupUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ABOUT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_CONTACT_CARD_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_EMPTY_PROMPT_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_OVERFLOW_MENU_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_PROGRESS_DIALOG_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_RINGTONE_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEmptyPromptUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsContentTest {

    @Test
    fun whenLoaded_showsTheHeaderAndBothCards() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithTag(CONTACT_DETAILS_HEADER_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_CONTACT_CARD_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CONTACT_DETAILS_ABOUT_CARD_TEST_TAG).assertExists()
        onNodeWithText("About Anna").assertExists()
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
            aboutCard = persistentListOf(),
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
                    aboutCard = persistentListOf(),
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
        onNodeWithTag(CONTACT_DETAILS_OVERFLOW_MENU_TEST_TAG).assertDoesNotExist()
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

        setContentWith(state = State(content = loadedContent(menu = menu)))

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(CONTACT_DETAILS_OVERFLOW_MENU_TEST_TAG).performClick()
        onNodeWithText("Delete").assertDoesNotExist()
    }

    @Test
    fun whenAMenuItemIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        onNodeWithTag(CONTACT_DETAILS_OVERFLOW_MENU_TEST_TAG).performClick()
        onNodeWithText("Share").performClick()

        assertEquals(listOf(Action.ShareClick), actions)
    }

    @Test
    fun whenTheRingtoneRowIsClicked_reportsIt() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setContentWith(
            state = State(content = loadedContent()),
            onAction = { action -> actions += action },
        )

        onNodeWithTag(CONTACT_DETAILS_RINGTONE_TEST_TAG).performClick()

        assertEquals(listOf(Action.RingtoneClick), actions)
    }

    @Test
    fun forAContactWithoutARingtoneOption_hidesTheRingtoneRow() = runComposeUiTest {
        val menu = contactDetailsMenu(isRingtoneVisible = false)

        setContentWith(state = State(content = loadedContent(menu = menu)))

        onNodeWithTag(CONTACT_DETAILS_RINGTONE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun theRingtoneIsNotInTheOverflowMenu() = runComposeUiTest {
        setContentWith(state = State(content = loadedContent()))

        onNodeWithTag(CONTACT_DETAILS_OVERFLOW_MENU_TEST_TAG).performClick()

        onNodeWithText("Set ringtone").assertDoesNotExist()
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
        aboutCard: ImmutableList<ContactEntryGroupUiModel> = persistentListOf(
            contactEntryGroupUiModel(
                entries = listOf(contactEntryUiModel(id = 2L, header = "Note", icon = null)),
            ),
        ),
        emptyPrompt: ContactDetailsEmptyPromptUiModel? = null,
        menu: ContactDetailsMenu = contactDetailsMenu(),
        isStarred: Boolean = false,
    ): Content.Loaded {
        return Content.Loaded(
            header = contactHeaderUiModel(displayName = "Anna Smith"),
            contactCard = contactCard,
            aboutCard = aboutCard,
            aboutCardTitle = "About Anna",
            emptyPrompt = emptyPrompt,
            menu = menu,
            isStarred = isStarred,
        )
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
}
