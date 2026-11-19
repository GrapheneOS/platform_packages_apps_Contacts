package com.android.contacts.ui.group.detail.screen

import android.net.Uri
import app.cash.turbine.test
import com.android.contacts.R
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.groups.model.GroupSendTargets
import com.android.contacts.group.GroupUtil
import com.android.contacts.ui.group.detail.GroupActivity
import com.android.contacts.ui.group.detail.screen.model.GroupAction as Action
import com.android.contacts.ui.group.detail.screen.model.GroupEffect as Effect
import com.android.contacts.ui.group.detail.screen.model.GroupState
import com.android.contacts.ui.selection.screen.model.SendToGroupTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GroupViewModelActionTest : BaseGroupViewModelTest() {

    // Dismiss

    @Test
    fun onAction_withDismissed_closesTheScreen() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.Dismissed)

            assertEquals(Effect.Close, awaitItem())
        }
    }

    @Test
    fun onAction_withDismissedWhileSelecting_clearsTheSelection() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))

        viewModel.effects.test {
            viewModel.onAction(Action.Dismissed)
            advanceUntilIdle()

            assertEquals(GroupState(), uiStateMapper.lastState)
            expectNoEvents()
        }
    }

    // Contact clicks and selection

    @Test
    fun onAction_withContactClicked_opensTheContact() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.ContactClicked(MEMBER_2.key))

            assertEquals(
                Effect.OpenContact(Uri.parse("content://com.android.contacts/contacts/2")),
                awaitItem(),
            )
        }
    }

    @Test
    fun onAction_withDirectoryContactClicked_doesNothing() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.ContactClicked(ContactItemKey.Directory(1L, 2L, "key")))
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun onAction_withContactClickedWhileSelecting_togglesTheSelection() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))

        viewModel.effects.test {
            viewModel.onAction(Action.ContactClicked(MEMBER_2.key))
            advanceUntilIdle()
            assertEquals(setOf(MEMBER_1.key, MEMBER_2.key), uiStateMapper.lastState?.selectedKeys)

            viewModel.onAction(Action.ContactClicked(MEMBER_1.key))
            advanceUntilIdle()
            assertEquals(setOf(MEMBER_2.key), uiStateMapper.lastState?.selectedKeys)

            expectNoEvents()
        }
    }

    @Test
    fun onAction_withContactLongClicked_togglesTheSelection() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))
        advanceUntilIdle()
        assertEquals(setOf(MEMBER_1.key), uiStateMapper.lastState?.selectedKeys)

        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))
        advanceUntilIdle()
        assertEquals(emptySet<ContactItemKey>(), uiStateMapper.lastState?.selectedKeys)
    }

    @Test
    fun onAction_withSelectAllClicked_selectsEveryMember() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(Action.SelectAllClicked)
        advanceUntilIdle()

        assertEquals(MEMBERS.map { it.key }.toSet(), uiStateMapper.lastState?.selectedKeys)
    }

    @Test
    fun onAction_withSelectAllClickedBeforeTheGroupLoads_doesNothing() = runTest {
        every { getGroup(any()) } returns emptyFlow()
        val viewModel = loadedViewModel()

        viewModel.onAction(Action.SelectAllClicked)
        advanceUntilIdle()

        assertEquals(GroupState(), uiStateMapper.lastState)
    }

    @Test
    fun onAction_withSelectionCleared_clearsTheSelection() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.SelectAllClicked)

        viewModel.onAction(Action.SelectionCleared)
        advanceUntilIdle()

        assertEquals(GroupState(), uiStateMapper.lastState)
    }

    // Create, add, rename

    @Test
    fun onAction_withCreateContactClicked_createsAContactInTheGroup() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.CreateContactClicked)

            assertEquals(Effect.CreateContact(GROUP), awaitItem())
        }
    }

    @Test
    fun onAction_withCreateContactClickedBeforeTheGroupLoads_doesNothing() = runTest {
        every { getGroup(any()) } returns emptyFlow()
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.CreateContactClicked)
            viewModel.onAction(Action.AddMembersClicked)
            viewModel.onAction(Action.RenameClicked)
            viewModel.onAction(Action.DeleteClicked)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun onAction_withAddMembersClicked_picksMembersExcludingTheExistingOnes() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.AddMembersClicked)

            assertEquals(
                Effect.PickMembers(
                    account = GROUP.account,
                    existingMemberIds = setOf(1L, 2L, 3L),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun onAction_withMembersPicked_addsThemToTheGroup() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.MembersPicked(setOf(4L, 5L)))
            advanceUntilIdle()

            coVerify {
                addGroupMembers(GROUP, setOf(4L, 5L), GroupActivity::class.java)
            }
            expectNoEvents()
        }
    }

    @Test
    fun onAction_withMembersPickedAndTheUpdateFails_showsAnError() = runTest {
        coEvery { addGroupMembers(any(), any(), any()) } returns false
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.MembersPicked(setOf(4L)))

            assertEquals(Effect.ShowMessage(R.string.groupSavedErrorToast), awaitItem())
        }
    }

    @Test
    fun onAction_withNoMembersPicked_doesNothing() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(Action.MembersPicked(emptySet()))
        advanceUntilIdle()

        coVerify(exactly = 0) { addGroupMembers(any(), any(), any()) }
    }

    @Test
    fun onAction_withRenameClicked_editsTheGroupName() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.RenameClicked)

            assertEquals(Effect.EditName(GROUP), awaitItem())
        }
    }

    // Send

    @Test
    fun onAction_withSendClicked_sendsToEveryMember() = runTest {
        coEvery { getGroupSendTargets(any(), any()) } returns
            GroupSendTargets.Direct(values = listOf("1", "2"), hasContactsWithoutValue = false)
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SendClicked(SendToGroupTarget.SMS))

            assertEquals(Effect.SendTo(SendToGroupTarget.SMS, listOf("1", "2")), awaitItem())
            coVerify { getGroupSendTargets(setOf(1L, 2L, 3L), ContactDataKind.Phone()) }
        }
    }

    @Test
    fun onAction_withSendClickedWhileSelecting_sendsToTheSelectedMembersAndClears() = runTest {
        coEvery { getGroupSendTargets(any(), any()) } returns
            GroupSendTargets.Direct(values = listOf("a@b.c"), hasContactsWithoutValue = false)
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_2.key))

        viewModel.effects.test {
            viewModel.onAction(Action.SendClicked(SendToGroupTarget.EMAIL))

            assertEquals(Effect.SendTo(SendToGroupTarget.EMAIL, listOf("a@b.c")), awaitItem())
            coVerify { getGroupSendTargets(setOf(2L), ContactDataKind.Email) }
            assertEquals(GroupState(), uiStateMapper.lastState)
        }
    }

    @Test
    fun onAction_withSendClickedAndSomeMembersWithoutValues_warnsAndSends() = runTest {
        coEvery { getGroupSendTargets(any(), any()) } returns
            GroupSendTargets.Direct(values = listOf("1"), hasContactsWithoutValue = true)
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SendClicked(SendToGroupTarget.SMS))
            assertEquals(Effect.ShowMessage(R.string.groupSomeContactsNoPhonesToast), awaitItem())
            assertEquals(Effect.SendTo(SendToGroupTarget.SMS, listOf("1")), awaitItem())

            viewModel.onAction(Action.SendClicked(SendToGroupTarget.EMAIL))
            assertEquals(Effect.ShowMessage(R.string.groupSomeContactsNoEmailsToast), awaitItem())
            assertEquals(Effect.SendTo(SendToGroupTarget.EMAIL, listOf("1")), awaitItem())
        }
    }

    @Test
    fun onAction_withSendClickedAndNoValues_showsAMessage() = runTest {
        coEvery { getGroupSendTargets(any(), any()) } returns GroupSendTargets.None
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SendClicked(SendToGroupTarget.SMS))
            assertEquals(
                Effect.ShowMessage(R.string.groupContactsWithoutPhonesToast),
                awaitItem(),
            )

            viewModel.onAction(Action.SendClicked(SendToGroupTarget.EMAIL))
            assertEquals(
                Effect.ShowMessage(R.string.groupContactsWithoutEmailsToast),
                awaitItem(),
            )
        }
    }

    @Test
    fun onAction_withSendClickedAndAmbiguousValues_picksTheTargets() = runTest {
        coEvery { getGroupSendTargets(any(), any()) } returns
            GroupSendTargets.NeedsPicker(contactIds = setOf(1L, 2L), defaultSelection = setOf(10L))
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SendClicked(SendToGroupTarget.EMAIL))

            assertEquals(
                Effect.PickSendTargets(
                    target = SendToGroupTarget.EMAIL,
                    contactIds = setOf(1L, 2L),
                    defaultSelection = setOf(10L),
                ),
                awaitItem(),
            )
        }
    }

    // Delete

    @Test
    fun onAction_withDeleteClicked_asksForConfirmation() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.DeleteClicked)
            advanceUntilIdle()

            assertEquals(GroupState(isDeleteConfirmationVisible = true), uiStateMapper.lastState)
            coVerify(exactly = 0) { deleteGroup(any()) }
            expectNoEvents()
        }
    }

    @Test
    fun onAction_withDeleteClickedOnAnEmptyGroup_deletesAndCloses() = runTest {
        members.value = emptyList()
        givenDeletionRemovesTheGroup()
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.DeleteClicked)

            assertEquals(Effect.Close, awaitItem())
            coVerify { deleteGroup(GROUP_ID) }
            assertEquals(GroupState(), uiStateMapper.lastState)
        }
    }

    @Test
    fun onAction_withDeleteConfirmed_hidesTheDialogDeletesAndCloses() = runTest {
        givenDeletionRemovesTheGroup()
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.DeleteClicked)

        viewModel.effects.test {
            viewModel.onAction(Action.DeleteConfirmed)

            assertEquals(Effect.Close, awaitItem())
            coVerify { deleteGroup(GROUP_ID) }
            assertEquals(GroupState(), uiStateMapper.lastState)
        }
    }

    @Test
    fun onAction_withDeleteConfirmed_whenNothingIsDeleted_staysOpen() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.DeleteClicked)

        viewModel.effects.test {
            viewModel.onAction(Action.DeleteConfirmed)
            advanceUntilIdle()

            coVerify { deleteGroup(GROUP_ID) }
            expectNoEvents()
        }
    }

    @Test
    fun onAction_withDeleteDismissed_hidesTheDialog() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.DeleteClicked)

        viewModel.onAction(Action.DeleteDismissed)
        advanceUntilIdle()

        assertEquals(GroupState(), uiStateMapper.lastState)
        coVerify(exactly = 0) { deleteGroup(any()) }
    }

    // Remove

    @Test
    fun onAction_withRemoveSelectedClicked_removesTheSelectedMembersAndClears() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))
        viewModel.onAction(Action.ContactLongClicked(MEMBER_3.key))

        viewModel.effects.test {
            viewModel.onAction(Action.RemoveSelectedClicked)
            advanceUntilIdle()

            coVerify {
                removeGroupMembers(GROUP, setOf(1L, 3L), GroupActivity::class.java)
            }
            assertEquals(GroupState(), uiStateMapper.lastState)
            expectNoEvents()
        }
    }

    @Test
    fun onAction_withRemoveSelectedClickedAndTheUpdateFails_showsAnError() = runTest {
        coEvery { removeGroupMembers(any(), any(), any()) } returns false
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))

        viewModel.effects.test {
            viewModel.onAction(Action.RemoveSelectedClicked)

            assertEquals(Effect.ShowMessage(R.string.groupSavedErrorToast), awaitItem())
        }
    }

    @Test
    fun onAction_withRemoveSelectedClickedWithoutSelection_doesNothing() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(Action.RemoveSelectedClicked)
        advanceUntilIdle()

        coVerify(exactly = 0) { removeGroupMembers(any(), any(), any()) }
    }

    @Test
    fun onAction_withRemoveSelectedClickedForContactsNoLongerInTheGroup_ignoresThem() = runTest {
        val viewModel = loadedViewModel()
        viewModel.onAction(Action.ContactLongClicked(MEMBER_1.key))
        viewModel.onAction(Action.ContactLongClicked(MEMBER_2.key))
        advanceUntilIdle()
        members.value = listOf(MEMBER_2, MEMBER_3)
        advanceUntilIdle()

        viewModel.onAction(Action.RemoveSelectedClicked)
        advanceUntilIdle()

        coVerify { removeGroupMembers(GROUP, setOf(2L), GroupActivity::class.java) }
    }

    // Save completed

    @Test
    fun onAction_withSaveCompleted_showsTheMatchingMessage() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SaveCompleted(GroupUtil.ACTION_UPDATE_GROUP, true))
            assertEquals(Effect.ShowMessage(R.string.groupUpdatedToast), awaitItem())

            viewModel.onAction(Action.SaveCompleted(GroupUtil.ACTION_ADD_TO_GROUP, true))
            assertEquals(Effect.ShowMessage(R.string.groupMembersAddedToast), awaitItem())

            viewModel.onAction(Action.SaveCompleted(GroupUtil.ACTION_REMOVE_FROM_GROUP, true))
            assertEquals(Effect.ShowMessage(R.string.groupMembersRemovedToast), awaitItem())
        }
    }

    @Test
    fun onAction_withSaveCompletedUnsuccessfully_showsAnError() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SaveCompleted(GroupUtil.ACTION_UPDATE_GROUP, false))

            assertEquals(Effect.ShowMessage(R.string.groupSavedErrorToast), awaitItem())
        }
    }

    @Test
    fun onAction_withSaveCompletedForAnUnknownAction_doesNothing() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(Action.SaveCompleted(GroupUtil.ACTION_CREATE_GROUP, true))
            viewModel.onAction(Action.SaveCompleted(null, true))
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    // Simulates the provider: once the group is deleted, a null is sent
    private fun givenDeletionRemovesTheGroup() {
        coEvery { deleteGroup(GROUP_ID) } answers { groupDetails.value = null }
    }
}
