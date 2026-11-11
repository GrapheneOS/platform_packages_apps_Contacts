package com.android.contacts.ui.selection.screen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import com.android.contacts.R
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_DESCRIPTION_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_EMPTY_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_LIST_HEADER_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_LOADING_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_BACK_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG
import com.android.contacts.ui.selection.screen.model.CONTACT_SELECTION_TITLE_TEST_TAG
import com.android.contacts.ui.selection.screen.model.ContactListSection as Section
import com.android.contacts.ui.selection.screen.model.ContactSelectionAction as Action
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionListState
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState as UiState
import com.android.contacts.ui.selection.screen.model.ContactUiModel
import com.android.contacts.ui.selection.screen.model.contactSelectionContactTestTag
import com.android.contacts.ui.selection.screen.model.contactSelectionSectionTestTag
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionScreenTest {

    private val actions = mutableListOf<Action>()

    // Top app bar

    @Test
    fun showsTitle() = runComposeUiTest {
        setContent(UiState(title = TITLE))

        onNodeWithTag(CONTACT_SELECTION_TITLE_TEST_TAG).assertTextEquals(TITLE)
    }

    @Test
    fun closeButton_sendsCloseClick() = runComposeUiTest {
        setContent(UiState(title = TITLE))

        onNodeWithTag(CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.CloseClick), actions)
    }

    @Test
    fun searchButton_whenSearchIsAvailable_sendsSearchClick() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Regular))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.SearchClick), actions)
    }

    @Test
    fun searchButton_whenSearchIsUnavailable_isHidden() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Unavailable))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun submitButton_whenSingleSelection_isHidden() = runComposeUiTest {
        setContent(UiState(allowMultiple = false))

        onNodeWithTag(CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun submitButton_withoutSelection_isDisabled() = runComposeUiTest {
        setContent(UiState(allowMultiple = true, selectedCount = 0))

        onNodeWithTag(CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun submitButton_withSelection_sendsSubmitClick() = runComposeUiTest {
        setContent(UiState(allowMultiple = true, selectedCount = 2))

        onNodeWithTag(CONTACT_SELECTION_SUBMIT_BUTTON_TEST_TAG)
            .assertIsEnabled()
            .assertTextContains(string(R.string.contactPickerDoneButton))
            .performClick()

        assertEquals(listOf(Action.SubmitClick), actions)
    }

    // Search

    @Test
    fun searching_replacesTitleAndButtonsWithSearchField() = runComposeUiTest {
        setContent(UiState(title = TITLE, searchState = SearchState.Searching("john")))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG)
            .assertIsDisplayed()
            .assertTextEquals("john")
        onNodeWithTag(CONTACT_SELECTION_TITLE_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(CONTACT_SELECTION_CLOSE_BUTTON_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(CONTACT_SELECTION_SEARCH_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun searchField_whenTyping_sendsQueryChange() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Searching("")))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG).performTextInput("john")
        waitForIdle()

        assertEquals(listOf(Action.SearchQueryChange("john")), actions)
    }

    @Test
    fun searchField_withoutQuery_hasNoClearButton() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Searching("")))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun searchClearButton_clearsQuery() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Searching("john")))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG).performClick()
        waitForIdle()

        onNodeWithTag(CONTACT_SELECTION_SEARCH_FIELD_TEST_TAG)
            .assert(hasEditableText(""))
            .assertTextContains(string(R.string.searchHint))
        onNodeWithTag(CONTACT_SELECTION_SEARCH_CLEAR_BUTTON_TEST_TAG).assertDoesNotExist()
        assertEquals(listOf(Action.SearchQueryChange("")), actions)
    }

    @Test
    fun searchBackButton_sendsCloseClick() = runComposeUiTest {
        setContent(UiState(searchState = SearchState.Searching("john")))

        onNodeWithTag(CONTACT_SELECTION_SEARCH_BACK_BUTTON_TEST_TAG).performClick()

        assertEquals(listOf(Action.CloseClick), actions)
    }

    // Content

    @Test
    fun description_whenPresent_isShown() = runComposeUiTest {
        setContent(UiState(description = DESCRIPTION, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_DESCRIPTION_TEST_TAG).assertTextEquals(DESCRIPTION)
    }

    @Test
    fun description_whenAbsent_isHidden() = runComposeUiTest {
        setContent(UiState(description = null, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_DESCRIPTION_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whileLoading_showsLoadingIndicatorOnly() = runComposeUiTest {
        setContent(
            UiState(
                emptyMessageId = R.string.noContacts,
                listState = ContactSelectionListState(isLoading = true),
            ),
        )

        onNodeWithTag(CONTACT_SELECTION_LOADING_TEST_TAG).assertExists()
        onNodeWithTag(CONTACT_SELECTION_EMPTY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun withoutContacts_showsEmptyMessage() = runComposeUiTest {
        setContent(UiState(emptyMessageId = R.string.noContacts, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_EMPTY_TEST_TAG)
            .assertTextEquals(string(R.string.noContacts))
        onNodeWithTag(CONTACT_SELECTION_LOADING_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun withoutContactsAndMessage_showsNoEmptyMessage() = runComposeUiTest {
        setContent(UiState(emptyMessageId = null, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_EMPTY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun withContacts_showsNoEmptyMessage() = runComposeUiTest {
        setContent(UiState(emptyMessageId = R.string.noContacts, listState = contactList(ALICE)))

        onNodeWithTag(CONTACT_SELECTION_EMPTY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun listHeader_whenPresent_isShown() = runComposeUiTest {
        setContent(UiState(listHeader = "2 contacts", listState = contactList(ALICE)))

        onNodeWithTag(CONTACT_SELECTION_LIST_HEADER_TEST_TAG).assertTextEquals("2 contacts")
    }

    @Test
    fun listHeader_whenAbsent_isHidden() = runComposeUiTest {
        setContent(UiState(listHeader = null, listState = contactList(ALICE)))

        onNodeWithTag(CONTACT_SELECTION_LIST_HEADER_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun createButton_whenAllowed_sendsCreateClick() = runComposeUiTest {
        setContent(UiState(canCreate = true, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG)
            .assertTextContains(string(R.string.header_entry_contact_list_adapter_header_title))
            .performClick()

        assertEquals(listOf(Action.CreateClick), actions)
    }

    @Test
    fun createButton_whenNotAllowed_isHidden() = runComposeUiTest {
        setContent(UiState(canCreate = false, listState = LOADED_EMPTY))

        onNodeWithTag(CONTACT_SELECTION_CREATE_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    // Sections and contacts

    @Test
    fun sections_showHeaderAndContacts() = runComposeUiTest {
        setContent(
            UiState(
                listState = listState(
                    Section.Favorites to listOf(ALICE),
                    Section.Alphabetical("A") to listOf(ALICE),
                    Section.Alphabetical("B") to listOf(BOB),
                ),
            ),
        )

        onNodeWithTag(contactSelectionSectionTestTag(Section.Favorites))
            .assertContainsText(string(R.string.contactsFavoritesLabel))
        onNodeWithTag(contactSelectionSectionTestTag(Section.Alphabetical("A")))
            .assertContainsText("A")
        onNodeWithTag(contactSelectionSectionTestTag(Section.Alphabetical("B")))
            .assertContainsText("B")
        onNodeWithTag(contactTag(Section.Favorites, ALICE)).assertIsDisplayed()
        onNodeWithTag(contactTag(Section.Alphabetical("A"), ALICE)).assertIsDisplayed()
        onNodeWithTag(contactTag(Section.Alphabetical("B"), BOB)).assertIsDisplayed()
    }

    @Test
    fun undefinedSection_hasNoHeader() = runComposeUiTest {
        setContent(UiState(listState = contactList(ALICE)))

        onNodeWithTag(contactSelectionSectionTestTag(Section.Undefined)).assertDoesNotExist()
        onNodeWithTag(contactTag(Section.Undefined, ALICE)).assertIsDisplayed()
    }

    @Test
    fun directorySection_showsDirectoryName() = runComposeUiTest {
        val section = Section.Directory(directoryId = 10L, name = "Work Directory")
        val contact = ContactUiModel(
            key = ContactItemKey.Directory(directoryId = 10L, id = 7L, lookupKey = "lookup"),
            name = "Remote Contact",
        )
        setContent(UiState(listState = listState(section to listOf(contact))))

        onNodeWithTag(contactSelectionSectionTestTag(section)).assertContainsText("Work Directory")
        onNodeWithTag(contactTag(section, contact)).performClick()

        assertEquals(listOf(Action.ContactClick(contact.key)), actions)
    }

    @Test
    fun contact_showsNameAndDescription() = runComposeUiTest {
        val contact = ContactUiModel(
            key = ContactItemKey.Local(3L),
            name = "Carl Jones",
            description = "Mobile · +1 555 0100",
        )
        setContent(UiState(listState = contactList(contact)))

        onNodeWithTag(contactTag(Section.Undefined, contact))
            .assertContainsText("Carl Jones")
            .assertContainsText("Mobile · +1 555 0100")
    }

    @Test
    fun contactClick_sendsContactClick() = runComposeUiTest {
        setContent(UiState(listState = contactList(ALICE, BOB)))

        onNodeWithTag(contactTag(Section.Undefined, BOB)).performClick()

        assertEquals(listOf(Action.ContactClick(BOB.key)), actions)
    }

    @Test
    fun contact_inSingleSelection_isClickableButNotToggleable() = runComposeUiTest {
        setContent(UiState(allowMultiple = false, listState = contactList(ALICE)))

        contactRow(contactTag(Section.Undefined, ALICE)).assertDoesNotExist()
        onNode(hasParent(hasTestTag(contactTag(Section.Undefined, ALICE))))
            .assertHasClickAction()
    }

    @Test
    fun contact_inMultipleSelection_reflectsSelection() = runComposeUiTest {
        val selectedBob = BOB.copy(isSelected = true)
        setContent(UiState(allowMultiple = true, listState = contactList(ALICE, selectedBob)))

        contactRow(contactTag(Section.Undefined, ALICE)).assertIsOff()
        contactRow(contactTag(Section.Undefined, selectedBob)).assertIsOn().performClick()

        assertEquals(listOf(Action.ContactClick(BOB.key)), actions)
    }

    // Screen

    @Test
    fun screen_rendersStateAndForwardsActions() = runComposeUiTest {
        val screenModel = FakeScreenModel(UiState(title = TITLE, listState = contactList(ALICE)))
        setContent { ContactSelectionScreen(mockk(relaxed = true), screenModel = screenModel) }

        onNodeWithTag(CONTACT_SELECTION_TITLE_TEST_TAG).assertTextEquals(TITLE)
        onNodeWithTag(contactTag(Section.Undefined, ALICE)).performClick()

        assertEquals(listOf(Action.ContactClick(ALICE.key)), screenModel.actions)
    }

    @Test
    fun screen_updatesWhenStateChanges() = runComposeUiTest {
        val screenModel = FakeScreenModel(UiState(title = TITLE))
        setContent { ContactSelectionScreen(mockk(relaxed = true), screenModel = screenModel) }

        screenModel.uiState.value = UiState(title = "2 selected")

        onNodeWithTag(CONTACT_SELECTION_TITLE_TEST_TAG).assertTextEquals("2 selected")
    }

    @Test
    fun screen_passesEffectsToHandler() = runComposeUiTest {
        val effectHandler = mockk<ContactSelectionEffectHandler>(relaxed = true)
        val screenModel = FakeScreenModel(UiState())
        setContent { ContactSelectionScreen(effectHandler, screenModel = screenModel) }

        val effect = Effect.Close(Result.Canceled)
        screenModel.effectsChannel.trySend(effect)
        waitForIdle()

        verify { effectHandler.handle(effect) }
    }

    private fun ComposeUiTest.setContent(state: UiState) {
        setContent {
            ContactSelectionContent(state = state, onAction = { actions += it })
        }
    }

    // Section headers and contact cells keep their text in child nodes
    private fun SemanticsNodeInteraction.assertContainsText(text: String) =
        assert(hasAnyDescendant(hasText(text)))

    private fun hasEditableText(text: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    // The toggle semantics live on the row inside the tagged cell
    private fun ComposeUiTest.contactRow(tag: String): SemanticsNodeInteraction {
        return onNode(isToggleable() and hasParent(hasTestTag(tag)))
    }

    private fun contactTag(section: Section, contact: ContactUiModel): String {
        return contactSelectionContactTestTag(section, contact.key)
    }

    private fun string(id: Int): String = targetContext.getString(id)

    private class FakeScreenModel(
        initialState: UiState,
    ) : ContactSelectionScreenModel {
        val actions = mutableListOf<Action>()
        val effectsChannel = Channel<Effect>(Channel.UNLIMITED)
        override val effects: Flow<Effect> = effectsChannel.receiveAsFlow()
        override val uiState = MutableStateFlow(initialState)

        override fun onAction(action: Action) {
            actions += action
        }
    }

    private companion object {
        const val TITLE = "Choose a contact"
        const val DESCRIPTION = "Choose the contact you want to link with Jane Doe:"
        val ALICE = ContactUiModel(key = ContactItemKey.Local(1L), name = "Alice Smith")
        val BOB = ContactUiModel(key = ContactItemKey.Local(2L), name = "Bob Brown")
        val LOADED_EMPTY = ContactSelectionListState(isLoading = false)

        fun contactList(vararg contacts: ContactUiModel): ContactSelectionListState {
            return listState(Section.Undefined to contacts.toList())
        }

        fun listState(
            vararg sections: Pair<Section, List<ContactUiModel>>,
        ): ContactSelectionListState {
            return ContactSelectionListState(
                contacts = sections
                    .associate { (section, contacts) -> section to contacts.toImmutableList() }
                    .toImmutableMap(),
            )
        }
    }
}
