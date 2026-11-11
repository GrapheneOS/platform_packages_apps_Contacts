package com.android.contacts.ui.selection.screen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.ContactSelectionFilter
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.domain.contacts.usecase.GetContact
import com.android.contacts.domain.contacts.usecase.GetContactsForSelection
import com.android.contacts.model.Contact
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.selection.ContactSelectionActivity
import com.android.contacts.ui.selection.screen.ContactSelectionViewModel.Companion.SEARCH_DEBOUNCE_MILLIS
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionEffectMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionFilterMapper
import com.android.contacts.ui.selection.screen.mapper.ContactSelectionUiStateMapper
import com.android.contacts.ui.selection.screen.model.ContactSelectionAction as Action
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactSelectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getContactsForSelection = mockk<GetContactsForSelection>()
    private val getContact = mockk<GetContact>()
    private val filterMapper = mockk<ContactSelectionFilterMapper>()
    private val effectMapper = mockk<ContactSelectionEffectMapper>()

    private val uiMapperCalls = mutableListOf<MapperCall>()
    private val uiStateMapper =
        ContactSelectionUiStateMapper { request, state, contacts, joinTarget ->
            uiMapperCalls += MapperCall(request, state, contacts, joinTarget)
            MAPPED_UI_STATE
        }
    private val lastUiMapperCall get() = uiMapperCalls.last()

    @Before
    fun setUp() {
        every { filterMapper.map(any()) } returns FILTER
        every { getContactsForSelection(any(), any()) } returns flowOf(RESULT)
        coEvery { effectMapper.map(any(), any()) } returns MAPPED_EFFECT
    }

    // Request

    @Test
    fun withoutRequest_closesCanceled() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel(request = null)

        subject.effects.test {
            assertEquals(Effect.Close(Result.Canceled), awaitItem())
        }
    }

    @Test
    fun withoutRequest_keepsDefaultUiState() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel(request = null)
        collectUiState(subject)

        assertEquals(ContactSelectionUiState(), subject.uiState.value)
        assertTrue(uiMapperCalls.isEmpty())
    }

    @Test
    fun uiState_isMappedFromRequest() = runTest(mainDispatcherRule.testDispatcher) {
        val request = ContactSelectionRequest(mode = Mode.PickEmail(), titleOverride = "Title")
        val subject = createViewModel(request)
        collectUiState(subject)

        assertEquals(MAPPED_UI_STATE, subject.uiState.value)
        assertEquals(request, lastUiMapperCall.request)
    }

    // Initial state

    @Test
    fun initialState_withPick_isRegularSearch() = runTest(mainDispatcherRule.testDispatcher) {
        collectUiState(createViewModel(Mode.Pick()))

        assertEquals(
            ContactSelectionState(searchState = SearchState.Regular),
            lastUiMapperCall.state,
        )
    }

    @Test
    fun initialState_withoutSearchSupport_isSearchUnavailable() =
        runTest(mainDispatcherRule.testDispatcher) {
            collectUiState(createViewModel(Mode.PickPostal()))

            assertEquals(SearchState.Unavailable, lastUiMapperCall.state.searchState)
        }

    @Test
    fun initialState_withPickSearchQuery_isSearching() =
        runTest(mainDispatcherRule.testDispatcher) {
            collectUiState(createViewModel(Mode.Pick(searchQuery = "john")))

            assertEquals(SearchState.Searching("john"), lastUiMapperCall.state.searchState)
            verify { getContactsForSelection(FILTER, "john") }
        }

    @Test
    fun initialState_withSendToGroup_selectsDefaultSelection() =
        runTest(mainDispatcherRule.testDispatcher) {
            val mode = Mode.SendToGroup(
                target = SentToGroupTarget.SMS,
                itemIds = listOf(1L, 2L, 3L),
                defaultSelection = listOf(1L, 3L),
                chooserTitle = null,
            )

            collectUiState(createViewModel(mode))

            assertEquals(setOf(local(1L), local(3L)), lastUiMapperCall.state.selectedKeys)
        }

    // Contacts

    @Test
    fun contacts_areQueriedWithFilterForMode() = runTest(mainDispatcherRule.testDispatcher) {
        val mode = Mode.PickPhoneNumber()

        collectUiState(createViewModel(mode))

        verify { filterMapper.map(mode) }
        verify(exactly = 1) { getContactsForSelection(FILTER, null) }
        assertEquals(null to RESULT, lastUiMapperCall.contacts)
    }

    @Test
    fun contacts_beforeResults_areNull() = runTest(mainDispatcherRule.testDispatcher) {
        every { getContactsForSelection(any(), any()) } returns emptyFlow()

        collectUiState(createViewModel())

        assertNull(lastUiMapperCall.contacts)
    }

    @Test
    fun searchQueryChange_updatesStateRightAway() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()
        collectUiState(subject)

        subject.onAction(Action.SearchQueryChange("john"))
        runCurrent()

        assertEquals(SearchState.Searching("john"), lastUiMapperCall.state.searchState)
    }

    @Test
    fun searchQueryChange_isDebounced() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()
        collectUiState(subject)

        subject.onAction(Action.SearchQueryChange("john"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()

        verify(exactly = 0) { getContactsForSelection(FILTER, "john") }

        advanceTimeBy(2)
        runCurrent()

        verify(exactly = 1) { getContactsForSelection(FILTER, "john") }
        assertEquals("john" to RESULT, lastUiMapperCall.contacts)
    }

    @Test
    fun searchQueryChange_onlyQueriesLatestTrimmedQuery() =
        runTest(mainDispatcherRule.testDispatcher) {
            val subject = createViewModel()
            collectUiState(subject)

            subject.onAction(Action.SearchQueryChange("j"))
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS / 2)
            subject.onAction(Action.SearchQueryChange(" john "))
            advanceUntilIdle()

            verify(exactly = 0) { getContactsForSelection(FILTER, "j") }
            verify(exactly = 1) { getContactsForSelection(FILTER, "john") }
        }

    @Test
    fun clearingSearchQuery_queriesRightAway() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel(Mode.Pick(searchQuery = "john"))
        collectUiState(subject)

        subject.onAction(Action.SearchQueryChange(" "))
        runCurrent()

        verify(exactly = 1) { getContactsForSelection(FILTER, null) }
    }

    @Test
    fun searchClick_withoutQueryChange_doesNotQueryAgain() =
        runTest(mainDispatcherRule.testDispatcher) {
            val subject = createViewModel()
            collectUiState(subject)

            subject.onAction(Action.SearchClick)
            advanceUntilIdle()

            assertEquals(SearchState.Searching(""), lastUiMapperCall.state.searchState)
            verify(exactly = 1) { getContactsForSelection(any(), any()) }
        }

    // Join target

    @Test
    fun join_loadsTargetContact() = runTest(mainDispatcherRule.testDispatcher) {
        val contact = mockk<Contact>()
        every { getContact(7L) } returns flowOf(contact)

        collectUiState(createViewModel(Mode.Join(targetContactId = 7L)))

        assertSame(contact, lastUiMapperCall.joinTarget)
    }

    @Test
    fun otherModes_doNotLoadJoinTarget() = runTest(mainDispatcherRule.testDispatcher) {
        collectUiState(createViewModel(Mode.Pick()))

        assertNull(lastUiMapperCall.joinTarget)
        verify(exactly = 0) { getContact(any()) }
    }

    // Close

    @Test
    fun closeClick_whileSearching_stopsSearching() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()
        collectUiState(subject)

        subject.effects.test {
            subject.onAction(Action.SearchClick)
            subject.onAction(Action.CloseClick)
            advanceUntilIdle()

            assertEquals(SearchState.Regular, lastUiMapperCall.state.searchState)
            expectNoEvents()
        }
    }

    @Test
    fun closeClick_whenNotSearching_closesCanceled() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()

        subject.effects.test {
            subject.onAction(Action.CloseClick)

            assertEquals(Effect.Close(Result.Canceled), awaitItem())
        }
    }

    // Contact click

    @Test
    fun contactClick_inMultipleMode_togglesSelection() =
        runTest(mainDispatcherRule.testDispatcher) {
            val subject = createViewModel(Mode.Pick(multiple = true))
            collectUiState(subject)

            subject.onAction(Action.ContactClick(local(1L)))
            subject.onAction(Action.ContactClick(local(2L)))
            runCurrent()
            assertEquals(setOf(local(1L), local(2L)), lastUiMapperCall.state.selectedKeys)

            subject.onAction(Action.ContactClick(local(1L)))
            runCurrent()
            assertEquals(setOf(local(2L)), lastUiMapperCall.state.selectedKeys)
            coVerify(exactly = 0) { effectMapper.map(any(), any()) }
        }

    @Test
    fun contactClick_inMultipleModeWithDirectoryContact_finishesWithContact() =
        runTest(mainDispatcherRule.testDispatcher) {
            val mode = Mode.Pick(multiple = true)
            val subject = createViewModel(mode)

            subject.effects.test {
                subject.onAction(Action.ContactClick(DIRECTORY_KEY))

                assertEquals(MAPPED_EFFECT, awaitItem())
            }
            coVerify { effectMapper.map(mode, setOf(DIRECTORY_KEY)) }
        }

    @Test
    fun contactClick_inSingleMode_finishesWithContact() =
        runTest(mainDispatcherRule.testDispatcher) {
            val mode = Mode.PickEmail()
            val subject = createViewModel(mode)

            subject.effects.test {
                subject.onAction(Action.ContactClick(local(1L)))

                assertEquals(MAPPED_EFFECT, awaitItem())
            }
            coVerify { effectMapper.map(mode, setOf(local(1L))) }
        }

    @Test
    fun finishing_onlyHappensOnce() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()

        subject.effects.test {
            subject.onAction(Action.ContactClick(local(1L)))
            subject.onAction(Action.ContactClick(local(2L)))
            subject.onAction(Action.SubmitClick)

            assertEquals(MAPPED_EFFECT, awaitItem())
            expectNoEvents()
        }
        coVerify(exactly = 1) { effectMapper.map(any(), any()) }
    }

    // Submit

    @Test
    fun submitClick_finishesWithSelectedContacts() = runTest(mainDispatcherRule.testDispatcher) {
        val mode = Mode.Pick(multiple = true)
        val subject = createViewModel(mode)

        subject.effects.test {
            subject.onAction(Action.ContactClick(local(1L)))
            subject.onAction(Action.ContactClick(local(2L)))
            subject.onAction(Action.SubmitClick)

            assertEquals(MAPPED_EFFECT, awaitItem())
        }
        coVerify { effectMapper.map(mode, setOf(local(1L), local(2L))) }
    }

    @Test
    fun submitClick_withoutSelection_finishesWithEmptySelection() =
        runTest(mainDispatcherRule.testDispatcher) {
            val mode = Mode.Pick(multiple = true)
            val subject = createViewModel(mode)

            subject.effects.test {
                subject.onAction(Action.SubmitClick)

                assertEquals(MAPPED_EFFECT, awaitItem())
            }
            coVerify { effectMapper.map(mode, emptySet()) }
        }

    // Other actions

    @Test
    fun createClick_forwardsToEditor() = runTest(mainDispatcherRule.testDispatcher) {
        val subject = createViewModel()

        subject.effects.test {
            subject.onAction(Action.CreateClick)

            assertEquals(Effect.ForwardToEditor.CreateContact, awaitItem())
        }
    }

    @Test
    fun state_isRestoredFromSavedState() = runTest(mainDispatcherRule.testDispatcher) {
        val savedStateHandle = savedStateHandle(ContactSelectionRequest(Mode.Pick(multiple = true)))
        val first = createViewModel(savedStateHandle)
        first.onAction(Action.ContactClick(local(1L)))
        first.onAction(Action.SearchQueryChange("john"))

        collectUiState(createViewModel(savedStateHandle))

        assertEquals(
            ContactSelectionState(
                searchState = SearchState.Searching("john"),
                selectedKeys = setOf(local(1L)),
            ),
            lastUiMapperCall.state,
        )
    }

    private fun TestScope.collectUiState(subject: ContactSelectionViewModel) {
        backgroundScope.launch { subject.uiState.collect {} }
        advanceUntilIdle()
    }

    private fun createViewModel(mode: Mode = Mode.Pick()): ContactSelectionViewModel {
        return createViewModel(ContactSelectionRequest(mode = mode))
    }

    private fun createViewModel(request: ContactSelectionRequest?): ContactSelectionViewModel {
        return createViewModel(savedStateHandle(request))
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle): ContactSelectionViewModel {
        return ContactSelectionViewModel(
            savedStateHandle = savedStateHandle,
            getContactsForSelection = getContactsForSelection,
            getContact = getContact,
            filterMapper = filterMapper,
            uiStateMapper = uiStateMapper,
            effectMapper = effectMapper,
        )
    }

    private fun savedStateHandle(request: ContactSelectionRequest?): SavedStateHandle {
        return SavedStateHandle(
            request?.let { mapOf(ContactSelectionActivity.EXTRA_REQUEST to it) }.orEmpty(),
        )
    }

    private fun local(id: Long): ContactItemKey.Local {
        return ContactItemKey.Local(id)
    }

    private data class MapperCall(
        val request: ContactSelectionRequest,
        val state: ContactSelectionState,
        val contacts: Pair<String?, GetContactsForSelectionResult>?,
        val joinTarget: Contact?,
    )

    private companion object {
        val FILTER = ContactSelectionFilter.Contacts(includesDirectories = true)
        val RESULT = GetContactsForSelectionResult(
            contacts = listOf(ContactItem(key = ContactItemKey.Local(1L), name = "John Smith")),
        )
        val MAPPED_UI_STATE = ContactSelectionUiState(title = "Mapped")
        val MAPPED_EFFECT = Effect.Close(Result.GroupMember(99L))
        val DIRECTORY_KEY = ContactItemKey.Directory(
            directoryId = 10L,
            id = 7L,
            lookupKey = "remote-lookup",
        )
    }
}
