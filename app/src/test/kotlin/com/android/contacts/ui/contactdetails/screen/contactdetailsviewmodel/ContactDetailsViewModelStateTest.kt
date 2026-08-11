package com.android.contacts.ui.contactdetails.screen.contactdetailsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}
