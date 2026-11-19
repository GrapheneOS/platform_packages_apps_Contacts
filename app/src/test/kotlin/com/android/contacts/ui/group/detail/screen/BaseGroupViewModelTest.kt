package com.android.contacts.ui.group.detail.screen

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.groups.model.GroupDetails
import com.android.contacts.domain.groups.model.GroupSendTargets
import com.android.contacts.domain.groups.usecase.AddGroupMembers
import com.android.contacts.domain.groups.usecase.DeleteGroup
import com.android.contacts.domain.groups.usecase.GetGroup
import com.android.contacts.domain.groups.usecase.GetGroupMembers
import com.android.contacts.domain.groups.usecase.GetGroupSendTargets
import com.android.contacts.domain.groups.usecase.RemoveGroupMembers
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.ContactItemFactory
import com.android.contacts.tests.factory.GroupDetailsFactory
import com.android.contacts.tests.factory.GroupFactory
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.detail.screen.mapper.GroupUiStateMapper
import com.android.contacts.ui.group.detail.screen.model.GroupContentState
import com.android.contacts.ui.group.detail.screen.model.GroupState
import com.android.contacts.ui.group.detail.screen.model.GroupUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal abstract class BaseGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val getGroup = mockk<GetGroup>()
    protected val getGroupMembers = mockk<GetGroupMembers>()
    protected val getGroupSendTargets = mockk<GetGroupSendTargets>()
    protected val addGroupMembers = mockk<AddGroupMembers>()
    protected val removeGroupMembers = mockk<RemoveGroupMembers>()
    protected val deleteGroup = mockk<DeleteGroup>(relaxed = true)
    protected val uiStateMapper = RecordingUiStateMapper()

    protected val groupDetails = MutableStateFlow<GroupDetails?>(GROUP_DETAILS)
    protected val members = MutableStateFlow(MEMBERS)

    @Before
    fun setUpBase() {
        every { getGroup(any()) } returns groupDetails
        every { getGroupMembers(any()) } returns members
        coEvery { getGroupSendTargets(any(), any()) } returns GroupSendTargets.None
        coEvery { addGroupMembers(any(), any(), any()) } returns true
        coEvery { removeGroupMembers(any(), any(), any()) } returns true
    }

    protected fun createViewModel(groupUri: Uri? = GROUP_URI): GroupViewModel {
        return GroupViewModel(
            savedStateHandle = SavedStateHandle(mapOf(GroupActivity.EXTRA_GROUP_URI to groupUri)),
            getGroup = getGroup,
            getGroupMembers = getGroupMembers,
            getGroupSendTargets = getGroupSendTargets,
            addGroupMembers = addGroupMembers,
            removeGroupMembers = removeGroupMembers,
            deleteGroup = deleteGroup,
            uiStateMapper = uiStateMapper,
        )
    }

    protected fun TestScope.loadedViewModel(): GroupViewModel {
        val viewModel = createViewModel()
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        return viewModel
    }

    protected class RecordingUiStateMapper : GroupUiStateMapper {
        var lastState: GroupState? = null
            private set
        var lastContent: GroupContentState? = null
            private set

        override fun map(state: GroupState, content: GroupContentState): GroupUiState {
            lastState = state
            lastContent = content
            return when (content) {
                is GroupContentState.Loaded -> MAPPED_CONTENT
                else -> GroupUiState.Loading
            }
        }
    }

    protected companion object {
        const val GROUP_ID = 7L
        val GROUP_URI: Uri = Uri.parse("content://com.android.contacts/groups/$GROUP_ID")

        val GROUP = GroupFactory.build(id = GROUP_ID, name = "Friends")
        val GROUP_DETAILS = GroupDetailsFactory.build(group = GROUP)

        val MEMBER_1 = ContactItemFactory.build(key = ContactItemKey.Local(1L))
        val MEMBER_2 = ContactItemFactory.build(key = ContactItemKey.Local(2L))
        val MEMBER_3 = ContactItemFactory.build(key = ContactItemKey.Local(3L))
        val MEMBERS: List<ContactItem> = listOf(MEMBER_1, MEMBER_2, MEMBER_3)

        val MAPPED_CONTENT = GroupUiState.Content(
            title = "Friends",
            subtitle = null,
            contacts = persistentMapOf(),
            isSelectionMode = false,
            canAddMembers = true,
            canRemoveMembers = false,
            canEditGroup = true,
            canSend = true,
            canSelectAll = true,
            deleteConfirmationMessage = null,
        )
    }
}
