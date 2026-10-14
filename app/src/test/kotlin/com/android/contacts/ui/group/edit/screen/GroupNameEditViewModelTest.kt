package com.android.contacts.ui.group.edit.screen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.groups.usecase.CheckIfGroupNameExists
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupNameEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkIfGroupNameExists = mockk<CheckIfGroupNameExists>(relaxed = true)

    @Test
    fun whenDismissed_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            viewModel.effects.test {
                expectNoEvents()
                viewModel.onAction(Action.Dismissed)
                assertEquals(Effect.Close(isSuccessful = false), awaitItem())
            }
        }

    @Test
    fun createNewGroupSuccessfully() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenCheckIfGroupNameExists(false)
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(GroupNameEditActivity.EXTRA_ACCOUNT to account),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                assertEquals(
                    State(
                        isEdit = false,
                        name = "",
                    ),
                    expectMostRecentItem(),
                )
                viewModel.onAction(Action.NameChanged("Name"))
                advanceUntilIdle()
                assertEquals(
                    State(
                        isEdit = false,
                        name = "Name",
                    ),
                    expectMostRecentItem(),
                )
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                advanceUntilIdle()
            }

            viewModel.effects.test {
                assertEquals(
                    Effect.CreateGroup(
                        account = account,
                        groupName = "Name",
                    ),
                    expectMostRecentItem(),
                )
            }
        }

    @Test
    fun editExistingGroupSuccessfully() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenCheckIfGroupNameExists(false)
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Old Name",
                    GroupNameEditActivity.EXTRA_ACCOUNT to account,
                ),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                assertEquals(
                    State(
                        isEdit = true,
                        name = "Old Name",
                    ),
                    expectMostRecentItem(),
                )
                viewModel.onAction(Action.NameChanged("New Name"))
                advanceUntilIdle()
                assertEquals(
                    State(
                        isEdit = true,
                        name = "New Name",
                    ),
                    expectMostRecentItem(),
                )
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                advanceUntilIdle()
            }

            viewModel.effects.test {
                assertEquals(
                    Effect.EditGroup(
                        groupId = 123L,
                        groupName = "New Name",
                    ),
                    expectMostRecentItem(),
                )
            }
        }

    @Test
    fun onEdit_whenIsSameName_justCloseWithoutSaving() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Old Name",
                    GroupNameEditActivity.EXTRA_ACCOUNT to account,
                ),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccessful = true), awaitItem())
            }
        }

    @Test
    fun whenNameMatchesExisting_setInputError() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenCheckIfGroupNameExists(true)
            val account = AccountModelFactory.build()
            val viewModel = createViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(GroupNameEditActivity.EXTRA_ACCOUNT to account),
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.NameChanged("Existing Name"))
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                advanceUntilIdle()
                assertEquals(
                    State(
                        isEdit = false,
                        name = "Existing Name",
                        isDuplicatedName = true,
                    ),
                    expectMostRecentItem(),
                )
            }

            viewModel.effects.test {
                expectNoEvents()
            }
            coVerify { checkIfGroupNameExists(account, "Existing Name") }
        }

    @Test
    fun onProcessRestart_recoverCurrentName() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Original",
                ),
            )
            val originalViewModel = createViewModel(savedStateHandle)
            originalViewModel.uiState.test {
                advanceUntilIdle()
                originalViewModel.onAction(Action.NameChanged("New"))
                advanceUntilIdle()
                cancelAndIgnoreRemainingEvents()
            }

            createViewModel(savedStateHandle).uiState.test {
                advanceUntilIdle()
                assertEquals("New", expectMostRecentItem().name)
            }
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): GroupNameEditScreenModel {
        return GroupNameEditViewModel(
            savedStateHandle = savedStateHandle,
            checkIfGroupNameExists = checkIfGroupNameExists,
        )
    }

    private fun givenCheckIfGroupNameExists(exists: Boolean) {
        coEvery { checkIfGroupNameExists(any(), any()) } returns exists
    }
}
