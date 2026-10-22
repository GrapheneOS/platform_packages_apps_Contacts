package com.android.contacts.ui.interactions.callsubject

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.data.callsubject.model.CallSubjectConfiguration
import com.android.contacts.data.callsubject.repository.CallSubjectRepository
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectViewModel
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectAction as Action
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectArguments
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectEffect as Effect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallSubjectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val callSubjectRepository = mockk<CallSubjectRepository>(relaxed = true) {
        coEvery { getHistory() } returns emptyList()
    }

    @Test
    fun uiState_onArguments_isSet() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(
                    ContactAvatarImage.Uri(ARGUMENTS.thumbnailPhotoUri!!),
                    state.avatarImage,
                )
                assertEquals(ARGUMENTS.isBusiness, state.isBusiness)
                assertEquals(ARGUMENTS.nameOrNumber, state.name)
                assertEquals(ARGUMENTS.displayNumber, state.number)
                assertEquals(ARGUMENTS.numberLabel, state.numberLabel)
                assertEquals("", state.subject)
            }
        }

    @Test
    fun subject_onSavedStateHandle_isRestored() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                savedStateHandle = createSavedStateHandle(subject = "Running late"),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals("Running late", expectMostRecentItem().subject)
            }
        }

    @Test
    fun configuration_onStart_isLoaded() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                callSubjectConfiguration = CallSubjectConfiguration(
                    limit = 64,
                    encoding = Charsets.UTF_8,
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                val state = expectMostRecentItem()
                assertEquals(64, state.subjectLimit)
                assertEquals(Charsets.UTF_8, state.subjectEncoding)
            }
        }

    @Test
    fun history_onStart_isLoadedMostRecentFirst() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val history = listOf("Oldest", "Newest")
            coEvery { callSubjectRepository.getHistory() } returns history
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(history.reversed(), expectMostRecentItem().history)
            }
        }

    @Test
    fun history_whenEmpty_isNotAvailable() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertFalse(expectMostRecentItem().isHistoryAvailable)
            }
        }

    @Test
    fun subject_onSubjectChange_isUpdatedAndSaved() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle = savedStateHandle)

            viewModel.uiState.test {
                viewModel.onAction(Action.SubjectChange("Running late"))
                advanceUntilIdle()
                assertEquals("Running late", expectMostRecentItem().subject)
            }
            assertEquals(
                "Running late",
                savedStateHandle.get<String>(CallSubjectViewModel.KEY_SUBJECT),
            )
        }

    @Test
    fun subject_onHistorySubjectPick_isUpdated() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                viewModel.onAction(Action.HistorySubjectPick("Call me back"))
                advanceUntilIdle()
                assertEquals("Call me back", expectMostRecentItem().subject)
            }
        }

    @Test
    fun placeCall_onCallClick_isEmittedWithSubject() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAction(Action.SubjectChange("Running late"))

            viewModel.effects.test {
                viewModel.onAction(Action.CallClick)
                advanceUntilIdle()
                assertEquals(
                    Effect.PlaceCall(
                        subject = "Running late",
                        number = ARGUMENTS.number,
                        phoneAccountHandle = null,
                    ),
                    awaitItem(),
                )
            }
        }

    @Test
    fun subject_onCallClick_isAddedToHistory() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAction(Action.SubjectChange("Running late"))

            viewModel.onAction(Action.CallClick)
            advanceUntilIdle()

            coVerify(exactly = 1) { callSubjectRepository.addToHistory("Running late") }
        }

    @Test
    fun placeCall_onRepeatedCallClick_isEmittedOnce() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.CallClick)
                viewModel.onAction(Action.CallClick)
                advanceUntilIdle()
                assertTrue(awaitItem() is Effect.PlaceCall)
                expectNoEvents()
            }
        }

    @Test
    fun close_onDismiss_isEmitted() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                viewModel.onAction(Action.Dismiss)
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun avatarImage_withoutThumbnail_isNull() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(
                savedStateHandle = createSavedStateHandle(
                    arguments = ARGUMENTS.copy(thumbnailPhotoUri = null),
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                assertNull(expectMostRecentItem().avatarImage)
            }
        }

    private fun createSavedStateHandle(
        arguments: CallSubjectArguments = ARGUMENTS,
        subject: String? = null,
    ) = SavedStateHandle(
        buildMap {
            put(CallSubjectViewModel.EXTRA_ARGUMENTS, arguments)
            subject?.let { put(CallSubjectViewModel.KEY_SUBJECT, it) }
        },
    )

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = createSavedStateHandle(),
        callSubjectConfiguration: CallSubjectConfiguration = CallSubjectConfiguration(),
    ) = CallSubjectViewModel(
        savedStateHandle = savedStateHandle,
        getCallSubjectConfiguration = { callSubjectConfiguration },
        callSubjectRepository = callSubjectRepository,
    )

    companion object {
        private val ARGUMENTS = CallSubjectArguments(
            thumbnailPhotoUri = "content://com.android.contacts/data/1",
            isBusiness = true,
            nameOrNumber = "John Smith",
            number = "+12345678901",
            displayNumber = "+1 234-567-8901",
            numberLabel = "Work",
        )
    }
}
