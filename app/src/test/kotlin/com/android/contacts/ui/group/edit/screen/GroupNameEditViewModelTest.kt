package com.android.contacts.ui.group.edit.screen

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.domain.groups.model.Group
import com.android.contacts.domain.groups.usecase.CreateOrEditGroupName
import com.android.contacts.domain.groups.usecase.GetGroups
import com.android.contacts.group.GroupUtil
import com.android.contacts.tests.AccountModelFactory
import com.android.contacts.tests.GroupFactory
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.group.edit.GroupNameEditActivity
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditInputError
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupNameEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getGroups = mockk<GetGroups>(relaxed = true)
    private val createOrEditGroupName = mockk<CreateOrEditGroupName>(relaxed = true)

    @Test
    fun whenCallbackActivityIsNull_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenGroups()
            val viewModel = createViewModel()

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccessful = false), awaitItem())
            }
        }

    @Test
    fun whenDismissed_close() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenGroups()
            val viewModel = createViewModel(
                SavedStateHandle(
                    mapOf(
                        GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
                    ),
                ),
            )

            viewModel.effects.test {
                expectNoEvents()
                viewModel.onAction(Action.Dismissed)
                assertEquals(Effect.Close(isSuccessful = false), awaitItem())
            }
        }

    @Test
    fun createNewGroupSuccessfully() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenGroups()
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_ACCOUNT to account,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTION to GroupUtil.ACTION_CREATE_GROUP,
                ),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                assertEquals(State.Loading, awaitItem())
                advanceUntilIdle()
                assertEquals(
                    State.Ready(
                        isEdit = false,
                        name = "",
                        maxLenght = MAX_LENGHT,
                        inputError = null,
                    ),
                    expectMostRecentItem(),
                )
                viewModel.onAction(Action.NameChanged("Name"))
                advanceUntilIdle()
                assertEquals(
                    State.Ready(
                        isEdit = false,
                        name = "Name",
                        maxLenght = MAX_LENGHT,
                        inputError = null,
                    ),
                    expectMostRecentItem(),
                )
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
            }

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccessful = true), expectMostRecentItem())
            }

            verify {
                createOrEditGroupName(
                    account = account,
                    groupName = "Name",
                    groupId = null,
                    callbackActivity = PeopleActivity::class.java,
                    callbackAction = GroupUtil.ACTION_CREATE_GROUP,
                )
            }
        }

    @Test
    fun editExistingGroupSuccessfully() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenGroups()
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Old Name",
                    GroupNameEditActivity.EXTRA_ACCOUNT to account,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTION to GroupUtil.ACTION_UPDATE_GROUP,
                ),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                assertEquals(State.Loading, awaitItem())
                advanceUntilIdle()
                assertEquals(
                    State.Ready(
                        isEdit = true,
                        name = "Old Name",
                        maxLenght = MAX_LENGHT,
                        inputError = null,
                    ),
                    expectMostRecentItem(),
                )
                viewModel.onAction(Action.NameChanged("New Name"))
                advanceUntilIdle()
                assertEquals(
                    State.Ready(
                        isEdit = true,
                        name = "New Name",
                        maxLenght = MAX_LENGHT,
                        inputError = null,
                    ),
                    expectMostRecentItem(),
                )
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
            }

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccessful = true), expectMostRecentItem())
            }

            verify {
                createOrEditGroupName(
                    account = account,
                    groupName = "New Name",
                    groupId = 123L,
                    callbackActivity = PeopleActivity::class.java,
                    callbackAction = GroupUtil.ACTION_UPDATE_GROUP,
                )
            }
        }

    @Test
    fun onEdit_whenIsSameName_justCloseWithoutSaving() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            givenGroups()
            val account = AccountModelFactory.build()
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Old Name",
                    GroupNameEditActivity.EXTRA_ACCOUNT to account,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTION to GroupUtil.ACTION_CREATE_GROUP,
                ),
            )
            val viewModel = createViewModel(savedStateHandle)

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.effects.test {
                assertEquals(Effect.Close(isSuccessful = true), expectMostRecentItem())
            }

            verify(exactly = 0) {
                createOrEditGroupName(any(), any(), any(), any(), any())
            }
        }

    @Test
    fun whenNameMatchesExisting_setInputError() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val group = GroupFactory.build(name = "Existing Name")
            givenGroups(listOf(group))
            val viewModel = createViewModel(
                SavedStateHandle(
                    mapOf(
                        GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
                    ),
                ),
            )

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.NameChanged("Existing Name"))
                advanceUntilIdle()
                viewModel.onAction(Action.OkClicked)
                advanceUntilIdle()
                assertEquals(
                    State.Ready(
                        isEdit = false,
                        name = "Existing Name",
                        maxLenght = MAX_LENGHT,
                        inputError = GroupNameEditInputError.DUPLICATED_NAME,
                    ),
                    expectMostRecentItem(),
                )
            }

            verify(exactly = 0) {
                createOrEditGroupName(any(), any(), any(), any(), any())
            }
        }

    @Test
    fun onProcessRestart_recoverCurrentName() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val group = GroupFactory.build(name = "Existing")
            givenGroups(listOf(group))
            val savedStateHandle = SavedStateHandle(
                mapOf(
                    GroupNameEditActivity.EXTRA_GROUP_ID to 123L,
                    GroupNameEditActivity.EXTRA_GROUP_NAME to "Original",
                    GroupNameEditActivity.EXTRA_CALLBACK_ACTIVITY to PeopleActivity::class.java,
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
                assertEquals(
                    "New",
                    (expectMostRecentItem() as State.Ready).name,
                )
            }
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): GroupNameEditScreenModel {
        return GroupNameEditViewModel(
            savedStateHandle = savedStateHandle,
            getGroups = getGroups,
            getGroupNameMaxLenght = { MAX_LENGHT },
            createOrEditGroupName = createOrEditGroupName,
        )
    }

    private fun givenGroups(groups: List<Group> = emptyList()): List<Group> {
        every { getGroups(any()) } returns flowOf(groups)
        return groups
    }

    companion object {
        private const val MAX_LENGHT = 40
    }
}
