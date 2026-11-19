package com.android.contacts.ui.group.detail.screen

import android.net.Uri
import app.cash.turbine.test
import com.android.contacts.R
import com.android.contacts.ui.group.detail.screen.model.GroupContentState
import com.android.contacts.ui.group.detail.screen.model.GroupEffect
import com.android.contacts.ui.group.detail.screen.model.GroupState
import com.android.contacts.ui.group.detail.screen.model.GroupUiState
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupViewModelStateTest : BaseGroupViewModelTest() {

    @Test
    fun uiState_beforeTheGroupLoads_isLoading() = runTest {
        every { getGroup(any()) } returns emptyFlow()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(GroupUiState.Loading, awaitItem())
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun uiState_whenTheGroupLoads_isTheMappedState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()

            assertEquals(MAPPED_CONTENT, expectMostRecentItem())
            assertEquals(
                GroupContentState.Loaded(GROUP_DETAILS, MEMBERS),
                uiStateMapper.lastContent,
            )
            assertEquals(GroupState(), uiStateMapper.lastState)
        }
        verify { getGroup(GROUP_ID) }
        verify { getGroupMembers(GROUP_ID) }
    }

    @Test
    fun uiState_whenTheMembersChange_isRemapped() = runTest {
        val viewModel = loadedViewModel()

        members.value = listOf(MEMBER_1)
        advanceUntilIdle()

        assertEquals(
            GroupContentState.Loaded(GROUP_DETAILS, listOf(MEMBER_1)),
            uiStateMapper.lastContent,
        )
        assertEquals(MAPPED_CONTENT, viewModel.uiState.value)
    }

    @Test
    fun uiState_whenTheGroupDisappears_isLoadingAndCloses() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            groupDetails.value = null
            advanceUntilIdle()

            assertEquals(GroupEffect.Close, awaitItem())
            assertEquals(GroupContentState.Deleted, uiStateMapper.lastContent)
            assertEquals(GroupUiState.Loading, viewModel.uiState.value)
        }
    }

    @Test
    fun init_whenTheGroupDisappearsAndMembersChange_closesOnce() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            groupDetails.value = null
            advanceUntilIdle()
            members.value = emptyList()
            advanceUntilIdle()

            assertEquals(GroupEffect.Close, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun init_withoutGroupUri_showsAnErrorAndCloses() = runTest {
        val viewModel = createViewModel(groupUri = null)

        viewModel.effects.test {
            assertEquals(GroupEffect.ShowMessage(R.string.groupLoadErrorToast, shouldSurviveClose = true), awaitItem())
            assertEquals(GroupEffect.Close, awaitItem())
        }
        verify(exactly = 0) { getGroup(any()) }
    }

    @Test
    fun init_withANonGroupUri_showsAnErrorAndCloses() = runTest {
        val viewModel = createViewModel(
            groupUri = Uri.parse("content://com.android.contacts/contacts/7"),
        )

        viewModel.effects.test {
            assertEquals(GroupEffect.ShowMessage(R.string.groupLoadErrorToast, shouldSurviveClose = true), awaitItem())
            assertEquals(GroupEffect.Close, awaitItem())
        }
        verify(exactly = 0) { getGroup(any()) }
    }

    @Test
    fun init_withAGroupUriWithoutAnId_showsAnErrorAndCloses() = runTest {
        val viewModel = createViewModel(
            groupUri = Uri.parse("content://com.android.contacts/groups/abc"),
        )

        viewModel.effects.test {
            assertEquals(GroupEffect.ShowMessage(R.string.groupLoadErrorToast, shouldSurviveClose = true), awaitItem())
            assertEquals(GroupEffect.Close, awaitItem())
        }
    }

    @Test
    fun init_withAnInvalidGroupUri_staysLoading() = runTest {
        val viewModel = createViewModel(groupUri = null)
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertEquals(GroupUiState.Loading, viewModel.uiState.value)
    }
}
