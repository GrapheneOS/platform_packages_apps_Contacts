package com.android.contacts.ui.contactdetails.screen.contactdetailsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactDetailsViewModelActionTest : BaseContactDetailsViewModelTest() {

    @Test
    fun onAction_withBackClick_closesTheScreen() = runTest {
        val viewModel = createViewModel()

        viewModel.navigationEvents.test {
            viewModel.onAction(ContactDetailsAction.BackClick)

            assertEquals(ContactDetailsNavEvent.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withStarClick_starsTheContact() = runTest {
        val viewModel = createViewModel().bindContact()
        viewModel.uiState.test {
            awaitItem()
            emitLoaded()
            awaitItem()

            viewModel.onAction(ContactDetailsAction.StarClick)
            advanceUntilIdle()

            coVerify { contactActionsRepository.setStarred(LOOKUP_URI, true) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withStarClickOnAStarredContact_removesTheStar() = runTest {
        loadedState.value = LOADED_CONTENT.copy(isStarred = true)
        val viewModel = createViewModel().bindContact()
        viewModel.uiState.test {
            awaitItem()
            emitLoaded()
            awaitItem()

            viewModel.onAction(ContactDetailsAction.StarClick)
            advanceUntilIdle()

            coVerify { contactActionsRepository.setStarred(LOOKUP_URI, false) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withStarClickBeforeTheContactLoads_doesNothing() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.StarClick)
        advanceUntilIdle()

        coVerify(exactly = 0) { contactActionsRepository.setStarred(any(), any()) }
    }

    @Test
    fun onAction_withEditClick_cachesTheContactAndOpensTheEditor() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.EditClick)

            assertEquals(ContactDetailsEffect.OpenEditor, awaitItem())
            verify { contactDetailsRepository.cacheLoadedContact() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withAddDetailsClick_opensTheEditor() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.AddDetailsClick)

            assertEquals(ContactDetailsEffect.OpenEditor, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withMenuClicks_emitsTheMatchingHostEffects() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.DeleteClick)
            assertEquals(ContactDetailsEffect.ConfirmDelete, awaitItem())

            viewModel.onAction(ContactDetailsAction.ShareClick)
            assertEquals(ContactDetailsEffect.ShareContact, awaitItem())

            viewModel.onAction(ContactDetailsAction.ShortcutClick)
            assertEquals(ContactDetailsEffect.CreateShortcut, awaitItem())

            viewModel.onAction(ContactDetailsAction.RingtoneClick)
            assertEquals(ContactDetailsEffect.PickRingtone, awaitItem())

            viewModel.onAction(ContactDetailsAction.JoinClick)
            assertEquals(ContactDetailsEffect.PickJoinTarget, awaitItem())

            viewModel.onAction(ContactDetailsAction.LinkedContactsClick)
            assertEquals(ContactDetailsEffect.ViewLinkedContacts, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withEntryClick_asksTheHostToPerformTheAction() = runTest {
        val action = ContactEntryAction.Call("4155551212")
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.EntryClick(action))

            assertEquals(ContactDetailsEffect.PerformEntryAction(action), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withCopyClick_asksTheHostToCopyTheText() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.CopyClick("Phone", "4155551212"))

            assertEquals(
                ContactDetailsEffect.CopyToClipboard(label = "Phone", text = "4155551212"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withSetDefaultClick_marksTheDataItemAsDefault() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.SetDefaultClick(42L))
        advanceUntilIdle()

        coVerify { contactActionsRepository.setSuperPrimary(42L) }
    }

    @Test
    fun onAction_withClearDefaultClick_clearsTheDefault() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.ClearDefaultClick(42L))
        advanceUntilIdle()

        coVerify { contactActionsRepository.clearPrimary(42L) }
    }

    @Test
    fun onAction_withARingtonePicked_savesIt() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.RingtonePicked("content://media/ringtone/7"))
        advanceUntilIdle()

        coVerify {
            contactActionsRepository.setRingtone(LOOKUP_URI, "content://media/ringtone/7")
        }
    }

    @Test
    fun onAction_withARingtonePickedBeforeBinding_doesNothing() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ContactDetailsAction.RingtonePicked(null))
        advanceUntilIdle()

        coVerify(exactly = 0) { contactActionsRepository.setRingtone(any(), any()) }
    }

    @Test
    fun onAction_withAJoinTargetPicked_asksTheHostToJoin() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.JoinTargetPicked(11L))

            assertEquals(ContactDetailsEffect.JoinContacts(11L), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withAJoinTargetPicked_showsTheLinkingProgress() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onAction(ContactDetailsAction.JoinTargetPicked(11L))

            assertEquals(ContactLinkOperation.LINK, awaitItem().linkProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
