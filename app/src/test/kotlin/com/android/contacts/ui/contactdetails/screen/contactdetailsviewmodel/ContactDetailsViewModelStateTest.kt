package com.android.contacts.ui.contactdetails.screen.contactdetailsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.ui.contactdetails.ContactDetailsActivity
import com.android.contacts.ui.contactdetails.screen.ContactDetailsViewModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactDetailsViewModelStateTest : BaseContactDetailsViewModelTest() {

    @Test
    fun uiState_beforeBinding_isLoading() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ContactDetailsContent.Loading, awaitItem().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheArgumentsSurviveProcessDeath_loadsWithoutRebinding() = runTest {
        createViewModel().bindContact()
        val restored = ContactDetailsViewModel(
            savedStateHandle = savedStateHandle,
            contactDetailsRepository = contactDetailsRepository,
            contactActionsRepository = contactActionsRepository,
            buildContactDetailsCards = buildContactDetailsCards,
            getContactDetailsMenu = getContactDetailsMenu,
            contactDetailsUiStateMapper = contactDetailsUiStateMapper,
            contactShortcutRepository = contactShortcutRepository,
            displaySettingsRepository = displaySettingsRepository,
        )

        restored.uiState.test {
            awaitItem()
            emitLoaded()

            assertEquals(LOADED_CONTENT, awaitItem().content)
            verify { contactDetailsRepository.observeContactDetails(LOOKUP_URI, emptySet()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactLoads_isTheMappedState() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            awaitItem()
            emitLoaded()

            assertEquals(LOADED_CONTENT, awaitItem().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactIsNotFound_isNotFound() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            awaitItem()
            results.emit(ContactDetailsResult.NotFound)

            assertEquals(ContactDetailsContent.NotFound, awaitItem().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactFailsToLoad_isError() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            awaitItem()
            results.emit(ContactDetailsResult.Error)

            assertEquals(ContactDetailsContent.Error, awaitItem().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactLoads_buildsTheCardsForThePrioritizedMimeType() = runTest {
        val viewModel = createViewModel()
        viewModel.bind(
            lookupUri = LOOKUP_URI,
            excludedMimeTypes = setOf("vnd.example/thing"),
            prioritizedMimeType = "vnd.example/priority",
            callbackActivity = ContactDetailsActivity::class.java,
        )
        val details = contactDetails()

        viewModel.uiState.test {
            awaitItem()
            emitLoaded(details)
            awaitItem()

            verify { buildContactDetailsCards(details, "vnd.example/priority") }
            verify {
                contactDetailsRepository.observeContactDetails(
                    LOOKUP_URI,
                    setOf("vnd.example/thing"),
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactLoads_mapsItWithTheDisplayOrderPreference() = runTest {
        every { displaySettingsRepository.observeDisplaySettings() } returns
            flowOf(DISPLAY_SETTINGS.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST))
        val viewModel = createViewModel().bindContact()
        val details = contactDetails()

        viewModel.uiState.test {
            awaitItem()
            emitLoaded(details)
            awaitItem()

            verify {
                contactDetailsUiStateMapper.map(
                    details = details,
                    cards = EMPTY_CARDS,
                    menu = any(),
                    displayOrder = DisplayOrder.FAMILY_NAME_FIRST,
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactLoadsAgain_readsTheDisplayOrderOnce() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            awaitItem()
            emitLoaded()
            awaitItem()
            emitLoaded(contactDetails(displayName = "Reloaded"))
            awaitItem()

            verify(exactly = 1) { displaySettingsRepository.observeDisplaySettings() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun linkProgress_withoutAPendingOperation_isEmpty() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertNull(awaitItem().linkProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun linkProgress_withAPendingSplit_showsUnlinking() = runTest {
        every { contactActionsRepository.getPendingLinkOperation() } returns
            ContactLinkOperation.UNLINK

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ContactLinkOperation.UNLINK, awaitItem().linkProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun linkProgress_whenLinkingCompletes_isDismissed() = runTest {
        every { contactActionsRepository.getPendingLinkOperation() } returns
            ContactLinkOperation.LINK
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            linkOperations.emit(ContactLinkOperation.LINK)

            assertNull(awaitItem().linkProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun navigationEvents_whenUnlinkingCompletes_closesTheScreen() = runTest {
        val viewModel = createViewModel()

        viewModel.navigationEvents.test {
            advanceUntilIdle()
            linkOperations.emit(ContactLinkOperation.UNLINK)

            assertEquals(ContactDetailsNavEvent.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun navigationEvents_whenLinkingCompletes_keepsTheScreenOpen() = runTest {
        val viewModel = createViewModel()

        viewModel.navigationEvents.test {
            advanceUntilIdle()
            linkOperations.emit(ContactLinkOperation.LINK)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheContactLoads_reportsTheShortcutUsageOnce() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            emitLoaded()
            advanceUntilIdle()
            emitLoaded()
            advanceUntilIdle()

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { contactShortcutRepository.reportShortcutUsed("lookup-key") }
    }
}
